JToDo - A Java To-Do List App (Coursework)
----

### Overview

This is a Java application created with Swing and AWT as UI framework.

The coursework requirements didn't specify what to make. The only requirement is to use SWT or Swing to create a Java GUI application.

So, here you go.

This app works as a client side app. 
For the server side, see the 
[JToDo-Server](https://github.com/MossTheFox/coursework-jtodo-server) project.

Screenshots:

![Main App](https://raw.githubusercontent.com/MossTheFox/coursework-jtodo/master/screenshots/JToDo-Client-1.png)

![Manage tasks](https://raw.githubusercontent.com/MossTheFox/coursework-jtodo/master/screenshots/JToDo-Client-2.png)

![About](https://raw.githubusercontent.com/MossTheFox/coursework-jtodo/master/screenshots/JToDo-Client-About.png)

### Features

These are some basic CRUD things, 
if you see them in the view of server-side. 
Creating a UI and syncing logic is what the client side does, which is what we've done with Java Swing and AWT.

* **Create Collection** - Create new collections of tasks.
* **Add Task** - Add new tasks to a collection.
* **Edit Task/Collection** - Edit an existing task or collection. Add description to a task if you want.
* **Delete Task/Collection** - Delete an existing task or collection.
* **Timer for Reminders** - Set a reminder for a task.
* **Sync Anywhere** - Sync all your data with the server.
  * All sync operations are done in the background without blocking the UI.
* **OAuth** - Login with QQ OAuth (or other OAuth providers, modify at backend if you want).
  * This is nice because nobody would have to remember their password.

### Installation

It's an Intellij IDEA project. Pull the repository and open it with Intellij IDEA, then do whatever you want. IDEA version should be at least 2021.3.3.

Notice that the SSL of the API server is not trusted by Java, so you need to add a certificate to your cacerts file. 
See comments in /org/mainapp/GlobalConst.java for more information.

You can modify the code to force it to launch in offline mode as well (meaning to skip the user authentication process). The failure of network requests won't crash the app, so you can ignore it when running in offline mode.

### Usage

This app was created to finish the coursework. So, Don't expect too much about it.

In fact, seeing this project as a practice of using Swing and AWT to create a GUI application would be good enough.

OK, in general, this app helps you to manage your tasks, and set alerts to remind you of them. All tasks sync with the server.

The server I hosted might be shut down at any time, so if you are looking for checking and modifying the code, go to the [JToDo-Server](https://github.com/MossTheFox/coursework-jtodo-server) project to get the server-side code. You can host them on your own server.

The authentication logic might need some change though, if you are hosting it on your own server.

Or, modify the code to force it to launch in offline mode.

### License

    This project is licensed under the MIT license.

Note: This project may include some bad practices, so don't rely on the code too much if you are using this project as some reference.