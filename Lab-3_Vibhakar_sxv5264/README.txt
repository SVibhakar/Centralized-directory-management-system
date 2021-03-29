//Name : Sejal Vibhakar
//ID : 1001765264

1. Import all the files in eclipse project

OR 

1. Compile all java classes via command line.

2. Run 1 instance of Server.java and 3 instances of Client.java
3. Clients should prompt for username input.
4. Enter command on client frame in the format COMMAND ARG1,ARG2
	Valid commands: CREATE dir             ====> creates dir
			CREATE dir/dir2        ====> creates dir2 under dir
			DELETE dir	       ====> deletes dir
			MOVE/RENAME dir,dir3   ====> moves/renames dir to dir3
			LIST dir	       ====> lists contents of dir
			SYNCDIR dirn1
			DESYNC dirn1
			UNDOCREATE dir.        ====> deletes dir
			UNDODELETE dir.        ====> creates dir
			UNDOMOVE/UNDORENAME dir,dir3   ====> moves/renames dir3 to dir
			UNDOSYNC dirn1

NOTE: make sure to have following empty dirs in order to run -> A,B,C,local