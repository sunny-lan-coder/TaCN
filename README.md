# Tyanide / TaCN
Teachassist custom client

# Features:
 - Sync with teachassist website
 - Store marks offline
 - Calculate average (even if not displayed on actual teachassist webpage)
 - Simulation (eg. see what average will be if marks/weights are changed)
 - Notifications (whenever a mark is updated)
 - Allows multiple profiles
 
# Stuff currently in progress:
 1. Preparing to port to android:
   - Porting to jsoup, which is supported for Android
   - Organizing code (separating UI and logic)
   
# Issues:
 - Teachassist is able to detect the bot somehow and block it if too many requests are made
  - Fix by using custom browser driver

# Stuff used:
 - Maven
 - JavaFX
 - HtmlUnit
 - JFoenix
