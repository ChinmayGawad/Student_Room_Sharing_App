const admin = require("firebase-admin");

const DATABASE_URL = "https://share-rooms-default-rtdb.asia-southeast1.firebasedatabase.app";
const appConfig = { databaseURL: DATABASE_URL };

if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    appConfig.credential = admin.credential.applicationDefault();
} else {
    try {
        const serviceAccount = require("./service-account.json");
        appConfig.credential = admin.credential.cert(serviceAccount);
    } catch (e) {
        console.error("Please provide service-account.json in server/ or GOOGLE_APPLICATION_CREDENTIALS");
        process.exit(1);
    }
}

admin.initializeApp(appConfig);
const db = admin.database();

const demoRooms = {
    "demo_room_1": {
        id: "demo_room_1",
        userId: "demo_owner_1",
        roomName: "Luxury Student Studio Near Campus",
        location: "University North Gate",
        description: "Type: Private Room\n\nSpacious studio apartment with high-speed WiFi, study desk, and 24/7 security. Perfect for university students.",
        price: "8500",
        deposit: "17000",
        imageUrls: ["https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80"],
        amenities: ["High-Speed WiFi", "Air Conditioning", "Fully Furnished", "Attached Bath"],
        roomBooked: false
    },
    "demo_room_2": {
        id: "demo_room_2",
        userId: "demo_owner_2",
        roomName: "Cozy Shared PG for Students",
        location: "Metro Station West",
        description: "Type: PG\n\nClean and affordable shared room with mess facility, daily housekeeping, and laundry service included.",
        price: "6000",
        deposit: "12000",
        imageUrls: ["https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80"],
        amenities: ["High-Speed WiFi", "Washing Machine", "Attached Bath"],
        roomBooked: false
    },
    "demo_room_3": {
        id: "demo_room_3",
        userId: "demo_owner_3",
        roomName: "Premium Private Single Room",
        location: "Tech Park Sector 5",
        description: "Type: Flat\n\nFully furnished single room in a modern 3BHK flat with balcony view, power backup, and modular kitchen access.",
        price: "12500",
        deposit: "25000",
        imageUrls: ["https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=800&q=80"],
        amenities: ["High-Speed WiFi", "Air Conditioning", "Balcony", "Fully Furnished"],
        roomBooked: false
    },
    "demo_room_4": {
        id: "demo_room_4",
        userId: "demo_owner_4",
        roomName: "Modern Female Student Flat",
        location: "College Green Avenue",
        description: "Type: Flat\n\nSafe and peaceful environment for female students with gym access, power backup, and close proximity to public transit.",
        price: "9000",
        deposit: "18000",
        imageUrls: ["https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80"],
        amenities: ["High-Speed WiFi", "Air Conditioning", "Fully Furnished"],
        roomBooked: true
    }
};

async function seedDatabase() {
    console.log("Clearing existing /Rooms...");
    await db.ref("Rooms").set(demoRooms);
    console.log("Successfully seeded demo rooms!");
    process.exit(0);
}

seedDatabase().catch((err) => {
    console.error("Seeding failed:", err);
    process.exit(1);
});
