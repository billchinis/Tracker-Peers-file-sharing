# Tracker-Peers-file-sharing
A simple local Tracker-Peer file sharing app written in Java using Sockets

# Tracker
Keeps track of all the Peers connected and the files that they have available for download.
Each peer needs to register to tracker and then login to be able to upload and download files.
The Tracker only communicates with the peers and then the file sharing is done P2P.

# Peer
Every Peer connects to the Tracker and makes every file in the `shared_directory` available for download from the other Peers.
To be able to download, the Peer has to register and login to the Tracker and then choose a file to download from the menu on the CMD.

# HowTo
We have to set the IP address of the Tracker (line 34 of the file `Peer.java`).

Compile the Tracker: `javac Tracker.java`

Compile the Peer: `javac Peer.java`

First of all we have to run the Tracker:
`java Tracker`

Then run each Peer:
`java Peer`

