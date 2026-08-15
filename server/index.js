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

    db.ref("Users")
        .once("value")
        .then((usersSnap) => {
            const promises = [];
            usersSnap.forEach((userSnap) => {
                const token = userSnap.child("fcmToken").val();
                if (token && userSnap.key !== room.userId) {
                    promises.push(
                        sendNotification(
                            token,
                            "New Room Available",
                            `${room.roomName || "A new room"} - ${room.price || ""}`,
                            { roomId }
                        )
                    );
                }
            });
            return Promise.allSettled(promises);
        })
        .then(() => console.log(`Room notifications sent`))
        .catch((err) => console.error("Room notify error:", err.message));
});

roomsRef.once("value", () => {
    roomsReady = true;
    console.log("Listening for new rooms...");
});

console.log("Notification server started.");
