# android-ipc-client-app
Client app to use IPC to communicate with remote app.

## Summary
Client app is supposed to take input from user and get respective response from Remote app. It supports mesesage echoing along with basic arithmatic operations (add, subtracts, multiply & pow) on user's input.

At backend, it uses Inter Process Communication (IPC) to connect and communicate with Remote app. Bound service is being used along with Messanger and Handler classes to facilitate this communication.

## Prerequisites
Remote app needs to be installed on same device before starting the communication as processing the request and sending back the response is soley and only Remote app's responsibility. 
