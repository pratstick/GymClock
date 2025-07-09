# GymClock - Workout Timer & Planner

A modern Android application for planning, timing, and tracking your workouts. GymClock is designed for weightlifters, HIIT enthusiasts, and anyone looking to structure their fitness routines efficiently.

## Features

- **Custom Interval Timers:** Set rest periods between sets with optional sound and vibration alerts.
- **Workout Planner:** Organize your weekly workout schedule, assign exercises to specific days, and set daily goals.
- **Predefined Splits:** Access curated workout programs inspired by reputable sources such as The Fitness Wiki.
- **Progress Tracking:** Log your workouts, track personal records, and monitor your progress over time.
- **Cloud Sync (Optional):** Synchronize your data across devices using Firebase.

## Installation

### Manual Build

Clone the repository and build the app using Gradle:

```bash
git clone https://github.com/yourusername/gymclock.git
cd gymclock
./gradlew assembleDebug
```

Install the generated APK on your Android device.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room (with Flow)
- **Architecture:** MVVM
- **Dependency Injection:** Hilt

## Contributing

Contributions are welcome. To contribute:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes with clear messages.
4. Push your branch and open a Pull Request.

Please ensure your code follows the existing style and includes relevant tests.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Acknowledgments

- Workout routines and program inspiration from [The Fitness Wiki](https://thefitness.wiki/routines/)
- Icons provided by [Material Design](https://material.io/resources/icons/)

