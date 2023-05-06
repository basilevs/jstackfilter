# Jstackfilter UI

Thin Swing wrapper of Jstackfilter.

![screenshot](images/screenshot.png)

# Capabilities
- Show filtered or unfiltered jstack dumps.
- Accept dumps from
  - Jps and Jstack combo
  - System clipboard
  - Any text file
- Interactively register more custom idle threads

#Usage
- The Jstackfilter UI is distributed as a single runnable JAR. Run it with `java -jar jstackfilter.jar`.
- To exit the application, hit Esc. Other usual exit methods are also supported.
- Java processes are listed in the topmost table. Select one to get it's thread dump.
- The text field at the bottom shows the current thread dump. Select threads with mouse or keyboard.
- All other capabilities are presented as the ugly buttons at the top of the window. They have tooltips with hotkeys and descriptions.
