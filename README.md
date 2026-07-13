# Video SDK Android Quickstart 

This is a Sample App of the Android Zoom Video SDK built with Kotlin.  

Use of this Sample App is subject to our [Terms of Use](https://www.zoom.com/en/trust/terms/).

## Prerequisites

### System Requirements
- **JDK 17** (required by build.gradle.kts)
- **Android SDK:**
  - minSdk: API 35 (Android 15)
  - targetSdk: API 37 (Android 15)
  - compileSdk: API 37
- **Android Studio** (latest stable)
- **Gradle** (included via Gradle wrapper)

### Zoom Video SDK
- Zoom Video SDK Core dependency
- SDK Key and SDK Secret from Zoom

### Core Dependencies (from build.gradle.kts)
- AndroidX: core-ktx, appcompat, activity, constraintlayout, lifecycle, monitor, junit.ktx
- Google Material: 1.12.0
- Retrofit2: 2.10.0 (HTTP client)
- Gson: JSON serialization
- JJWT: 0.12.6 (JWT token generation/validation)
- Kotlin Coroutines: 1.5.2
- Dotenv: 6.5.1 (environment configuration)
- Pretty-print: v2.0.8 (logging utility)
- Flexbox: 3.0.0 (layout)

### Required Permissions (Auto-declared in AndroidManifest.xml)
- `INTERNET` - Network access
- `RECORD_AUDIO` - Microphone for calls
- `FOREGROUND_SERVICE` - Background service execution
- `FOREGROUND_SERVICE_DATA_SYNC` - KeepAliveService
- `READ_PHONE_STATE` - Call state monitoring
- `READ_CALL_LOG` - Call history
- `POST_NOTIFICATIONS` - Push notifications
- `BIND_TELECOM_CONNECTION_SERVICE` - Phone integration
- `MANAGE_OWN_CALLS` - Call management
- `RECEIVE_BOOT_COMPLETED` - Boot completion events

### Device Requirements
- Android device or emulator running Android 15+ (API 35+)
- Microphone access (runtime permission requested)

## Installation

Clone this repo into your local environment:
```
$ git clone https://github.com/Ticorrian-Heard/android-videosdk-connectionservice-sample.git 
```

Once cloned, navigate to the 'videosdk-android-quickstart' directory

You can use the `studio .` command to open it in Android Studio.

In the `env-sample` file found in `app/src/main/assets`, you can choose to either enter an endpoint url to a backend server which handles JWT Token generation or you can leave the field blank and use the below steps for manually generating a token in the Android Studio terminal. Once your data is entered, rename this file to `env`. 

> :warning: **Do not store credentials in plain text on production environments**

## Configuration

For manually generating a JWT Token:
1. Ensure you are using JDK 16+ and your project language level is set to JDK 16+
2. In the terminal navigate to the utils folder with this command `cd app/src/main/java/com/zoomvideosdkkotlin/utils`
3. Compile the `TokenGenerator.java` with this command: `javac -cp "lib/*"  TokenGenerator.java`
4. Execute the file with this command with command line argument in this order: `java -cp "lib/*" TokenGenerator.java [topic] [role(0 or 1)] [sdk key] [sdk secret]`
5. A JWT Token will output to the console. From there Build and run the application and input the JWT into the app when prompted.

If you choose to use the apps APIClient, the request query parameters and body structure can be edited to match your servers requirements in the `ApiService.kt` file. The current request structure is as follows:
```
curl --location --request POST 'http://ENDPOINT_URL/zoomtoken?token=&name=&password=' \
--header 'Content-Type: application/json' \
--data '{                        
    "body": {                    
        "sessionName" = "",
        "role" = 0,
        "userIdentity" = "",
        "sessionkey" = "",
        "geo_regions" = "",
        "cloud_recording_option" = 0,
        "cloud_recording_election" = 0,
        "telemetry_tracking_id" = "",
        "video_webrtc_mode" = 0,
        "audio_webrtc_mode" = 0
    }
}'
```

## Usage
After entering either a JWTToken or Endpoint URL and configuring your Endpoint,  you can start the Sample App by clicking Build and Run!
