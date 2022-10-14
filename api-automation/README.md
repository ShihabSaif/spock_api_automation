Prerequisites
-------------
- JDK 7 or higher
- Maven use `mvnw` wrapper
- Gradle use `gradlew` wrapper
- Ant 1.7 or higher (for Ant build)

Building with Gradle
--------------------
Type:

    ./gradlew clean test

Downloaded files (including the Gradle distribution itself) will be stored in the Gradle user home directory (typically *user_home*`/.gradle`).

Building with Maven
-------------------
Type:
    
    ./mvnw clean test
    
Downloaded files will be stored in the local Maven repository (typically *user_home*`/.m2/repository`).

How to run test and check the test report
-----------------------------------------
1. After cloning the project, go to the directory "automation-task\api-automation"
2. Build the automation project with gradle.
3. After step 2, a report will be generated (A html file named 'index.html').
4. Check the report in "api-automation\build\reports\tests\test"