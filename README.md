Smart Pantry Manager
A Java Android app (Mobile App Development 700, Richfield) that helps reduce food waste. The user records the
ingredients they already have at home (their "pantry") and the app suggests only the recipes they can cook
right now, with no shopping trip required.
Features
Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date)
Pantry list screen (RecyclerView + custom adapter) bound to the SQLite database
21 pre-loaded recipes, seeded on first run
Suggested Recipes screen using the strict-matching rule: a recipe appears only if every ingredient is in the pantry in at least the required quantity
Matching is robust to simple differences: "Tomatoes" matches "tomato", 1 kg satisfies 500 g, duplicate pantry rows are added together
Recipe detail screen, Settings screen (expiring-soon alerts, default unit), bottom navigation
Input validation and an empty-state message when no recipes match
Optional bonus: separate "Almost there" list of recipes missing exactly one ingredient
Database choice: SQLite (SQLiteOpenHelper)
SQLite is built into Android, works offline, needs no account or server, and is ideal for private, single-user data
such as a pantry. Data is stored in a file on the device, so it persists after the app is closed and reopened.
Setup / run instructions
Install Android Studio (Hedgehog or newer) with an Android SDK 34 and JDK 17.
Clone the repository and choose File > Open, then select the `SmartPantryManager` folder.
Wait for the Gradle sync to finish.
Create an emulator (Device Manager, e.g. Pixel 6, API 34) or connect a phone with USB debugging.
Press Run (green triangle).
Unit tests for the matching rule: right-click `app/src/test/.../RecipeMatcherTest` > Run.
No Google Maps, mapping SDK or location permission is used.
