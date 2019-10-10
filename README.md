# android-ipc-client-app
Client app to use IPC to communicate with remote app.

## Summary
Client app is supposed to take input from user and get respective response from Remote app. It supports mesesage echoing along with basic arithmatic operations (add, subtracts, multiply & pow) on user's input.

At backend, it uses Inter Process Communication (IPC) to connect and communicate with Remote app. Bound service is being used along with Messanger and Handler classes to facilitate this communication.

## Prerequisites
Remote app needs to be installed on same device before starting the communication as processing the request and sending back the response is soley and only Remote app's responsibility. 

## Alternate Solutions
1. Use the `IntentService` to receive the input at Remote app end and send back the result via PendingIntent or Messanger or Broadcast Intent or ResultReceiver.
2. Use the `Broadcast Intent` to send & receive the data. `Intent Filters` can be defined by each application based on the type of data they receive.
3. Use `Content Provider` at Client app to store the data & access them at Remote App via `ContentResolver`.
4. We can directly access the activity of other application if they are runnning in same process, hence we can user the Intent to send the data back and forth.

Solution implemented in this app is by using `Bound Services` which optimizes the implementation as compared to above solutions: You don't need to declare any `Intent Filters` to receive data, you wake the Remote app only when Client app needs to communicate and sends backs to sleep once Client app is done with communication. Also, you don't need to worry about in which process these apps are running as we are not directly accessing their classes/methods. We are not storing the data at any common place like `Content Provider`, hence it removes the data security concern.
