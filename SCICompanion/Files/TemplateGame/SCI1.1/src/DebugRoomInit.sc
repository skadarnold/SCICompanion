;;; Sierra Script 1.0 - (do not remove this comment)
; This script contains code that can be run when debugging your game starting in a particular room.
(script# DEBUGROOM_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)

(public
	DebugRoomInit 0
)


;	
;	 Modify this function to set up inventory items or flags as necessary.
;	
;	 :param number roomNumber: The room that the game is starting in when being debugged.
;	
;	 Example::
;	
;	 	(150 ; The room number
;	 		; The ego must have the thing if he's in this room.
;	 		(gEgo set: INV_THETHING)
;	 	)
;	
;	 See also: :doc:`/debugging`
(procedure (DebugRoomInit roomNumber)
	(switch roomNumber
	)
)
