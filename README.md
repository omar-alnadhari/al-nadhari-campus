<div align="center">

# Al-Nadhari Campus

### A personal academic task planner for Android

Organize courses, manage tasks, track deadlines, and keep study priorities in one place.

**Android · Java · XML · SQLite · Gradle**

</div>

---

## Overview

**Al-Nadhari Campus** is a native Android application designed to make everyday study planning straightforward. It brings course organization and task management together in a clean, focused interface, with on-device SQLite storage and no account or server setup required.

The application provides a dashboard for tracking progress, a structured workflow for creating and managing tasks, and search tools for finding assignments when needed.

## Features

- **Dashboard:** See the total number of tasks and the counts of pending and completed tasks.
- **Task management:** Create, view, edit, and delete tasks with a title, description, due date, course, and priority.
- **Priority levels:** Organize tasks as Low, Medium, or High priority.
- **Progress tracking:** Mark tasks as Pending or Completed.
- **Course management:** Add, rename, view, and delete courses.
- **Data integrity:** Prevent deleting a course that still has associated tasks.
- **Task search:** Find tasks by title, description, or course name.
- **Deadline ordering:** View tasks ordered by due date.
- **Local persistence:** Keep application data in an SQLite database on the device.
- **Responsive lists and empty states:** Display tasks and courses using RecyclerView-based screens.

## Technology Stack

| Area | Technology |
| --- | --- |
| Platform | Android (native) |
| Language | Java 11 |
| User interface | XML layouts, Android Views, Material Components |
| Data storage | SQLite via `SQLiteOpenHelper` |
| Lists | RecyclerView, ViewHolder adapters |
| Navigation | Activities and explicit Intents |
| Build system | Gradle Kotlin DSL |
| Android Gradle Plugin | 9.4.1 |
| Gradle Wrapper | 9.6.0 |
| Minimum Android version | API 24 (Android 7.0) |
| Compile / target SDK | API 37 |

## Application Screens

| Screen | Responsibility |
| --- | --- |
| `MainActivity` | Dashboard, task statistics, and navigation |
| `AddEditTaskActivity` | Create new tasks and update existing ones |
| `TaskListActivity` | Browse tasks and open their details |
| `TaskDetailsActivity` | Review task details, change status, edit, or delete |
| `SearchTasksActivity` | Search across task and course information |
| `CoursesActivity` | Create, rename, list, and delete courses |

## Project Structure

```text
Al-Nadhari-Campus/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/alnadhari/campus/
│       │   │   ├── MainActivity.java
│       │   │   ├── AddEditTaskActivity.java
│       │   │   ├── TaskListActivity.java
│       │   │   ├── TaskDetailsActivity.java
│       │   │   ├── SearchTasksActivity.java
│       │   │   ├── CoursesActivity.java
│       │   │   ├── DatabaseHelper.java
│       │   │   ├── TaskAdapter.java
│       │   │   ├── CourseAdapter.java
│       │   │   └── DeleteConfirmDialog.java
│       │   └── res/
│       │       ├── layout/
│       │       ├── values/
│       │       └── drawable/
│       ├── test/java/com/alnadhari/campus/
│       │   └── TaskModelsTest.java
│       └── androidTest/java/com/alnadhari/campus/
│           └── DatabaseHelperInstrumentedTest.java
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### How It Works

The application uses an **Activity-based architecture**. Activities handle user interactions and screen navigation, while `TaskAdapter` and `CourseAdapter` bind records to RecyclerView items. `DatabaseHelper` centralizes the SQLite schema and operations for courses, tasks, search, and dashboard counts.

## Data Model

The application uses two related SQLite tables:

**`courses`**

| Column | Purpose |
| --- | --- |
| `id` | Primary key |
| `name` | Unique course name |

**`tasks`**

| Column | Purpose |
| --- | --- |
| `id` | Primary key |
| `title` | Task title |
| `description` | Task details |
| `course_id` | Foreign key referencing `courses.id` |
| `due_date` | Due date stored as `yyyy-MM-dd` |
| `priority` | Low, Medium, or High |
| `status` | Pending or Completed |

A course can contain multiple tasks. Foreign-key enforcement and `ON DELETE RESTRICT` protect linked tasks from being orphaned when a course is deleted.

## Getting Started

### Requirements

- Android Studio with support for the project's Gradle and Android Gradle Plugin versions
- Android SDK Platform 37 installed
- Android Emulator or a physical Android device running API 24 or later

### Run in Android Studio

1. Clone or download this repository.
2. Open the project root folder in **Android Studio** (the folder containing `settings.gradle.kts`).
3. Allow **Gradle Sync** to complete and install any missing SDK components if prompted.
4. Select an emulator or connected Android device.
5. Select the `app` configuration and click **Run**.

### Build from the command line

From the project root:

**Windows:**

```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux:**

```bash
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/` after a successful build.

> **Windows path tip:** Keep the project in a directory whose full path uses ASCII characters if the Android Gradle Plugin reports an unsupported project path.

## Testing & Quality Assurance

Al-Nadhari Campus includes automated tests for its Java data models and SQLite data layer. The following results were **run and verified in Android Studio** during development:

| Test suite | Environment | Verified result |
| --- | --- | --- |
| `TaskModelsTest` | Local JUnit unit tests (JVM) | **3 / 3 passed** |
| `DatabaseHelperInstrumentedTest` | Android Emulator (API 37) | **8 / 8 passed** |
| **Total** | | **11 / 11 passed** |

### What's covered

- **Java model tests:** Task and course model property storage, plus independence between task instances.
- **SQLite instrumentation tests:** Default course creation; course creation, editing, and deletion; task creation, updating, completion, and deletion; input and status validation; course–task deletion restrictions; searching by title, description, or course; and due-date ordering.
- **Test isolation:** Instrumented database tests use a fresh, in-memory SQLite database rather than modifying the application's existing task and course records.

### Running the tests

In Android Studio, open the indicated class and choose **Run** from its context menu:

- Unit tests: `app/src/test/java/com/alnadhari/campus/TaskModelsTest.java`
- Instrumented tests: `app/src/androidTest/java/com/alnadhari/campus/DatabaseHelperInstrumentedTest.java` (start an emulator or connect an Android device first).

Or run the suites from the project root using Gradle:

**Windows (PowerShell):**

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

**macOS / Linux:**

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

The instrumentation command requires a connected device or running emulator. These results report the tests executed, **not 100% coverage of the entire application**. UI interaction flows have not yet been fully automated.

## Data and Offline Use

Task and course records are stored on the device using SQLite (`al_nadhari_campus.db`). The current application does not require user accounts, an external API, or a hosted backend for its core features. Android system backup behavior may depend on the device and application backup settings.

## Development Status

The current version implements the core course and task-management workflows. The app has been launched on an Android emulator, and all 11 currently implemented automated tests have passed. This does not imply that every workflow has automated test coverage.

Potential future improvements include scheduled deadline reminders, additional automated tests, database migration support, and further accessibility enhancements.

## Application Details

- **App name:** Al-Nadhari Campus
- **Application ID:** `com.alnadhari.campus`
- **Version:** `1.0`

## Developer

**Eng. Omar Al-Nadhari**

