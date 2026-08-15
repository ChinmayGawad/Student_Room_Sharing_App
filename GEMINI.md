# Student Room Sharing App - Project Context & Architecture

This document serves as the primary context memory file for AI coding assistants working on the **Student Room Sharing App** repository.

---

## 🛠️ Technology Stack & Core Specifications

- **Language & Platform**: Kotlin / Android SDK 35 (`minSdk = 28`, `targetSdk = 35`, JVM 1.8 compatibility target)
- **UI Architecture**: Material Design 3, ViewBinding, ConstraintLayout, SwipeRefreshLayout, Glide
- **Backend & Realtime Data**: Firebase Authentication, Firebase Realtime Database, Firebase Cloud Messaging (FCM)
- **Payments**: Razorpay Checkout SDK (`com.razorpay:checkout:1.6.38`)
- **Image Storage Strategy**: Free Imgur API upload (`https://api.imgur.com/3/image`) + 75% quality lossy WebP fallback (~20–35 KB per photo) to avoid Firebase Storage billing
- **Architecture Pattern**: MVVM with `StateFlow`, `SharedFlow`, Kotlin Coroutines, and Repository Pattern

---

## 📁 Repository Structure & Package Sitemap

- `app/src/main/java/com/pgshare/studentroomsharingapp/`
  - `AddRoomActivity.kt`: Multi-step wizard for property owners to list and publish rooms.
  - `StudentDashboardActivity.kt`: Navigation host (Explore, Saved, Inbox, Profile, Settings).
  - `RoomDetailsActivity.kt`: Detailed view with ViewPager images, amenity chips, and "Book Now" / "Chat" CTAs.
  - `PaymentActivity.kt`: Razorpay Checkout integration handling room rent & deposit payments, updating DB reservation status (`isRoomBooked = true`, `bookedBy`, `paymentTxnId`).
  - `ChatActivity.kt`: Realtime student-to-owner messaging.
  - `MainActivity.kt`: User role routing (Owner vs Student).
  - `repository/FirebaseRepository.kt`: Centralized repository handling Firebase Auth, Realtime DB queries, WebP compression, and Imgur API uploads.
  - `viewmodel/`: ViewModels for Explore, AddRoom, Chat, Inbox, Profile, and RoomDetails.
  - `model/`: Data classes for `Room.kt`, `Message.kt`, `RecentChat.kt`, `UserHelper.kt`.

- `app/src/main/res/`
  - `values/colors.xml` & `values-night/colors.xml`: Modern Material 3 palette with Indigo `#4F46E5` primary and Slate `#0F172A` background.
  - `layout/`: `fragment_explore.xml`, `item_room.xml`, `activity_room_details.xml`, `activity_payment.xml`, `activity_chat.xml`.

- `server/`
  - `index.js`: Node.js server for FCM push notification handling.
  - `seed_demo_rooms.js`: Admin SDK script to reset and seed 4 demo student room listings.

---

## 🗄️ Firebase Realtime Database Schema

- `/Rooms/$roomId`:
  - `id`: String
  - `userId`: String
  - `roomName`: String
  - `location`: String
  - `description`: String (`"Type: Private Room\n\nDetailed description..."`)
  - `price`: String (`"8500"`)
  - `deposit`: String (`"17000"`)
  - `imageUrls`: List<String> (HTTPS URLs or WebP Base64 strings)
  - `amenities`: List<String>
  - `isRoomBooked`: Boolean
  - `bookedBy`: String (User ID)
  - `paymentTxnId`: String

- `/Users/$userId`:
  - `username`: String
  - `email`: String
  - `role`: String (`"student"` or `"owner"`)
  - `favorites/$roomId`: Room Object

- `/chats/$chatId/messages/$messageId`:
  - `messageId`: String
  - `message`: String
  - `senderId`: String
  - `timestamp`: Long

- `/inbox/$userId/$chatId`:
  - `chatRoomId`: String
  - `roomId`: String
  - `otherUserId`: String
  - `lastMessage`: String
  - `timestamp`: Long

---

## 🧪 Development & Build Commands

- **Run Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest --no-daemon
  ```

- **Build Debug APK**:
  ```bash
  ./gradlew assembleDebug --no-daemon
  ```

- **Reset & Seed Demo Rooms via Admin SDK**:
  ```bash
  cd server
  node seed_demo_rooms.js
  ```

- **Start Push Notification Server**:
  ```bash
  cd server
  npm start
  ```
