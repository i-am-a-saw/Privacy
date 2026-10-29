## Privacy

**Privacy** is an Android messenger that aims to secure your identity and chats. It focuses on data encryption and preventing unauthorized access to your account, messages and sensitive data.

## Status - in development

The current milestone is **<u>Foundation</u>** (milestone 0 of 8)  
The app now has 4 core modules:
 - **:core:model** (holds the domain models: User, Conversation, Message)
 - **:core:common** (dispatchers, clock and error interface)
 - **:core:design system** (colors, type and theme + base UI elements)
 - **:core:testing** (main dispatcher rule, test clock and test data builders)

**build-logic** is an included Gradle build with convention plugins (Android application, Android Library, Compose, JVM Library) that simplify maintaining the same configuration across modules.

## Design decisions

**Fixed brand palette instead of dymanic color.** Colors in this app carry security meaning  
(verified/unverified safety numbers, key-changing warnings). They must look the same on every device and have  
contrast I have tested, which dynamic color cannot guarantee. A fixed palette is also needed as a  
fallback for Android 11 and below (minSdk is 26).

## Stack

| Tool/Framework  | Used for      |
|-----------------|---------------|   
| Kotlin          | core language |
| Jetpack Compose | UI            |
| Room            | local storage |
| Hilt            | DI            |


## Author
- [i-am-a-saw](https://github.com/i-am-a-saw)

