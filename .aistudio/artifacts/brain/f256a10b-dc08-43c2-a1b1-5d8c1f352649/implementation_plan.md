# HabitFlow — Comprehensive Update & Enhancement Plan

This plan addresses notification testing, home screen widget visibility, multi-method authentication (Email, Google, and Phone OTP), and secure role-based access for the Administrator Control Panel.

---

## User Review & Confirmed Decisions

> [!IMPORTANT]
> **Confirmed Choices from Phase 1:**
> - **Authentication Methods**: Support multiple sign-in options: **Email/Password**, **Google Sign-In**, and **Phone Number with OTP**.
> - **Widget Drawer Styles**: Provide two distinct widgets in the phone's widget section:
>   1. **Full Goal Card Widget (4x2 / 3x2)**: Shows today's habit completion score, progress bar, and active streak.
>   2. **Compact Streak Bar Widget (4x1 / 2x1)**: Quick-glance streak flame, today's completion count, and instant app shortcut.
> - **Role & Admin Authorization**: Replace the arbitrary manual switch with automated role assignment on login. Verified administrator credentials (including `pklaw9855@gmail.com` and designated admin phone/email logins) automatically grant Administrator privileges to access the Admin Control Panel and user directory.
> - **Android 13+ Runtime Notification Permission**: Implement standard Jetpack Compose runtime `POST_NOTIFICATIONS` permission launcher so test alerts and daily scheduled habit reminders immediately appear in the status bar and lock screen.

---

## 1. Overview & Core Enhancements

1. **Android Runtime Notification Fix**:
   - On Android 13+ (API 33+) and Android 16 (API 36), notifications require dynamic runtime authorization.
   - We will implement `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` in the UI to request and verify `POST_NOTIFICATIONS` before dispatching reminders or test triggers.
   - Provide a clear permission status indicator and instant notification test button with high-importance sound and vibration.

2. **Dual Home Screen App Widgets**:
   - **Full Daily Goal Card (`HabitGoalCardWidgetProvider`)**: Sized for 3x2 / 4x2 cells with progress bar, completion ratio, streak badge, and tap target.
   - **Compact Streak Flame Bar (`HabitStreakBarWidgetProvider`)**: Sized for 2x1 / 4x1 cells for home screen top/bottom rows.
   - Configure individual `appwidget-provider` XML metadata files with `targetCellWidth`, `targetCellHeight`, `minResizeWidth`, and `widgetCategory="home_screen|keyguard"` so Android launcher automatically lists both widgets in the widget picker drawer.

3. **Multi-Method Authentication & Session Management**:
   - Dedicated **Auth & Login Flow**:
     - **Email & Password**: Register new accounts or log into existing ones with password validation.
     - **Google Sign-In**: Quick one-tap Google login simulation/credential persistence.
     - **Phone Number + OTP**: Phone input with interactive 6-digit verification code step.
   - Session store in Room `UserProfileEntity`: tracks `isLoggedIn`, `authProvider` ("EMAIL", "GOOGLE", "PHONE"), `userId`, `email`, `phoneNumber`, `displayName`, and `role`.
   - Sign Out action that safely returns to the login screen.

4. **Securing the Administrator Control Panel**:
   - Remove the arbitrary toggle switch.
   - Automatically assign `role: "ADMIN"` when logging in with designated administrator accounts (such as `pklaw9855@gmail.com` or admin phone numbers) or via an "Admin Login" credentials gateway.
   - Standard user accounts receive standard access with their personal dashboard and habits.
   - Administrators unlock the Admin Control Panel with system metrics, all registered users list, official habit templates library, and broadcast alerts.

---

## 2. Technical Architecture & Component Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI                              │
│  ┌──────────────┐   ┌──────────────┐   ┌─────────────┐  ┌────────────┐ │
│  │ Login/Auth   │──▶│ Dashboard    │──▶│ Analytics   │  │ Admin      │ │
│  │ (Email/Phone)│   │ (With Perms) │   │ (Month/Year)│  │ (Protected)│ │
│  └──────────────┘   └──────────────┘   └─────────────┘  └────────────┘ │
└───────────────────────────────┬────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        ViewModels & StateFlow                          │
│               HabitViewModel  •  AuthSessionState                      │
└───────────────────────────────┬────────────────────────────────────────┘
                                │
        ┌───────────────────────┴───────────────────────┐
        ▼                                               ▼
┌───────────────────────────────┐       ┌────────────────────────────────┐
│   Room Persistent Database    │       │     Android System Services    │
│  • user_profiles (Auth & RBAC)│       │  • NotificationManager (API 33)│
│  • habits & habit_logs        │       │  • Dual AppWidgetProviders:    │
│  • global_habit_templates     │       │    1. HabitGoalCardWidget      │
│  • activity_history           │       │    2. HabitStreakBarWidget     │
└───────────────────────────────┘       └────────────────────────────────┘
```

---

## 3. Implementation Steps

1. **Notification Permission Handler**:
   - Create `NotificationPermissionHelper.kt` with runtime permission check (`ContextCompat.checkSelfPermission`) and Compose launcher.
   - Update `DashboardScreen` and `ProfileScreen` test buttons to request permission dynamically if not granted, then post the test alert.
2. **Dual Home Screen Widgets**:
   - Create `widget_goal_card_info.xml` and `widget_streak_bar_info.xml`.
   - Create layout XMLs: `widget_goal_card.xml` and `widget_streak_bar.xml`.
   - Implement `HabitGoalCardWidgetProvider` and `HabitStreakBarWidgetProvider` in `AndroidManifest.xml`.
3. **Authentication Data Layer**:
   - Update `UserProfileEntity` with `phoneNumber`, `passwordHash`, `authProvider`, and `isLoggedIn`.
   - Update `UserProfileDao` and `HabitRepository` to handle login, registration, phone verification, and session persistence.
4. **Login & Registration UI**:
   - Build `AuthScreen.kt` featuring tabbed sign-in (Email, Google, Phone OTP), registration mode, input validation, and password hiding.
   - Wire root navigation in `MainActivity.kt`: if `currentSession == null`, display `AuthScreen`; once authenticated, load `HabitFlowApp`.
5. **Admin Control Panel Security**:
   - Enforce authentic role verification based on verified user email or admin credentials.
   - Remove the dummy toggle; show designated admin badge and all platform administration controls.
6. **Verification & Testing**:
   - Run unit/Robolectric tests and execute `compile_applet` to confirm successful build.
