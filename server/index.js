const admin = require("firebase-admin");

// ─── Firebase Admin SDK Initialization ──────────────────
const DATABASE_URL = "https://share-rooms-default-rtdb.asia-southeast1.firebasedatabase.app";
const appConfig = { databaseURL: DATABASE_URL };

if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    appConfig.credential = admin.credential.applicationDefault();
} else {
    try {
        const serviceAccount = require("./service-account.json");
        appConfig.credential = admin.credential.cert(serviceAccount);
    } catch (e) {
        console.error(
            "No service account found.\n" +
            "  Dev: place service-account.json in server/\n" +
            "  Production: set GOOGLE_APPLICATION_CREDENTIALS env var"
        );
        process.exit(1);
    }
}

admin.initializeApp(appConfig);

const db = admin.database();

// ─── Helpers ────────────────────────────────────────────

function sendNotification(token, title, body, data = {}) {
    if (!token) return Promise.resolve();
    const message = { token, notification: { title, body }, data };
    return admin
        .messaging()
        .send(message)
        .then(() => console.log(`Sent to ${token.substring(0, 10)}...`))
        .catch((err) => console.error("FCM error:", err.message));
}

// ─── Message Notifications ──────────────────────────────
// Structure: /inbox/{userId}/{chatRoomId}
// Listen at /inbox for new userIds, then each userId's inbox for new chat entries

console.log("Watching /inbox for new users...");
const inboxRef = db.ref("/inbox");
let inboxReady = false;

inboxRef.on("child_added", (userSnap) => {
    const userId = userSnap.key;
    let userInboxReady = false;
    const userInboxRef = db.ref(`/inbox/${userId}`);

    userInboxRef.on("child_added", (chatSnap) => {
        if (!inboxReady || !userInboxReady) return;

        const chatRoomId = chatSnap.key;
        const preview = chatSnap.val();

        db.ref(`Users/${userId}/fcmToken`)
            .once("value")
            .then((tokenSnap) => {
                const token = tokenSnap.val();
                if (!token) {
                    console.log(`No FCM token for user ${userId}`);
                    return;
                }
                return sendNotification(
                    token,
                    "New Message",
                    preview.lastMessage || "You have a new message",
                    { chatRoomId, roomId: preview.roomId || "" }
                );
            })
            .catch((err) => console.error("Message notify error:", err.message));
    });

    userInboxRef.once("value", () => {
        userInboxReady = true;
    });
});

inboxRef.once("value", () => {
    inboxReady = true;
    console.log("Listening for new messages...");
});

// ─── New Room Notifications ─────────────────────────────
// Structure: /Rooms/{roomId}

console.log("Watching /Rooms for new listings...");
const roomsRef = db.ref("/Rooms");
let roomsReady = false;

roomsRef.on("child_added", (snapshot) => {
    if (!roomsReady) return;

    const room = snapshot.val();
    const roomId = snapshot.key;
    console.log(`New room: ${roomId} - ${room.roomName || ""}`);

    const message = {
        topic: "rooms_all",
        notification: {
            title: "New Room Available",
            body: `${room.roomName || "A new room"} - ₹${room.price || ""}`
        },
        data: {
            roomId: roomId || ""
        }
    };

    admin.messaging().send(message)
        .then(() => console.log(`Room notification sent to topic: rooms_all for ${roomId}`))
        .catch((err) => console.error("Room topic notify error:", err.message));
});

roomsRef.once("value", () => {
    roomsReady = true;
    console.log("Listening for new rooms...");
});

// ─── HTTP API Server (Payment Verification & Health) ────
const http = require("http");
const crypto = require("crypto");

const server = http.createServer(async (req, res) => {
    // CORS headers
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

    if (req.method === "OPTIONS") {
        res.writeHead(204);
        res.end();
        return;
    }

    const url = new URL(req.url, `http://${req.headers.host}`);

    if (req.method === "GET" && url.pathname === "/health") {
        res.writeHead(200, { "Content-Type": "application/json" });
        res.end(JSON.stringify({ status: "ok", uptime: process.uptime(), timestamp: Date.now() }));
        return;
    }

    if (req.method === "POST" && url.pathname === "/api/verify-payment") {
        let body = "";
        req.on("data", (chunk) => { body += chunk; });
        req.on("end", async () => {
            try {
                const data = JSON.parse(body || "{}");
                const { roomId, paymentId, orderId, signature, studentUid } = data;

                if (!roomId || !paymentId || !studentUid) {
                    res.writeHead(400, { "Content-Type": "application/json" });
                    res.end(JSON.stringify({ success: false, error: "Missing required fields: roomId, paymentId, studentUid" }));
                    return;
                }

                // Verify cryptographic signature if secret is present in environment
                const keySecret = process.env.RAZORPAY_KEY_SECRET;
                if (keySecret && orderId && signature) {
                    const expectedSignature = crypto
                        .createHmac("sha256", keySecret)
                        .update(`${orderId}|${paymentId}`)
                        .digest("hex");

                    if (expectedSignature !== signature) {
                        res.writeHead(400, { "Content-Type": "application/json" });
                        res.end(JSON.stringify({ success: false, error: "Invalid Razorpay payment signature" }));
                        return;
                    }
                }

                // Atomic booking transaction via Firebase Admin SDK
                const roomRef = db.ref(`Rooms/${roomId}`);
                const txResult = await roomRef.transaction((room) => {
                    if (!room) return room;
                    if (room.roomBooked) return; // Abort if already booked
                    room.roomBooked = true;
                    room.bookedBy = studentUid;
                    room.paymentTxnId = paymentId;
                    return room;
                });

                if (txResult.committed) {
                    res.writeHead(200, { "Content-Type": "application/json" });
                    res.end(JSON.stringify({ success: true, message: "Room reserved successfully", roomId, paymentId }));
                } else {
                    res.writeHead(409, { "Content-Type": "application/json" });
                    res.end(JSON.stringify({ success: false, error: "Room is already booked or does not exist" }));
                }
            } catch (err) {
                res.writeHead(500, { "Content-Type": "application/json" });
                res.end(JSON.stringify({ success: false, error: err.message }));
            }
        });
        return;
    }

    res.writeHead(404, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ error: "Not found" }));
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Notification & Payment server running on port ${PORT}`);
});

