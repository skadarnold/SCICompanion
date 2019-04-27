;;; Sierra Script 1.0 - (do not remove this comment)
; The Timer script contains the classes for timers, allowing the user to set up timed events.
(script# 973)
(include sci.sh)
(use Main)
(use System)


;	
;	 A timer class that can count down in ticks, milliseconds, seconds, minutes or hours.
;	
;	 Example usage::
;	
;	 	; Make a timer to cue() the current object in 3 seconds
;	 	((Timer new:) set: self 3)
(class Timer of Object
	(properties
		cycleCnt -1
		seconds -1
		ticks -1
		lastTime -1
		client 0
	)
	
	(procedure (CueClient &tmp theClient)
		(= theClient client)
		(= client 0)
		(if (IsObject theClient)
			(if (theClient respondsTo: #timer) (theClient timer: 0))
			(if (theClient respondsTo: #cue) (theClient cue:))
		)
	)
	
	
	(method (new)
		(return (if (== self Timer) (super new:) else self))
	)
	
	(method (init theClient)
		(= client theClient)
		(gTimers add: self)
		(if (theClient respondsTo: #timer)
			(if (IsObject (theClient timer?))
				((theClient timer?) dispose:)
			)
			(theClient timer: self)
		)
	)
	
	(method (doit &tmp theLastTime)
		(cond 
			((!= cycleCnt -1) (if (not (-- cycleCnt)) (CueClient)))
			((!= seconds -1)
				(= theLastTime (GetTime gtTIME_OF_DAY))
				(if (!= lastTime theLastTime)
					(= lastTime theLastTime)
					(if (not (-- seconds)) (CueClient))
				)
			)
			((> (- gGameTime ticks) 0) (CueClient))
		)
	)
	
	(method (dispose)
		(if
		(and (IsObject client) (client respondsTo: #timer))
			(client timer: 0)
		)
		(= client 0)
	)
	
	;
	; .. function:: set(theClient theSeconds [theMinutes theHours])
	;
	; 	Sets the timer.
	;
	; 	:param heapPtr theClient: This object will have its cue() method called when the timer expires.
	; 	:param number theSeconds: The number of seconds for the timer.
	; 	:param number theMinutes: The number of minutes for the timer (optional).
	; 	:param number theHours: The number of hours for the timer (optional).
	;
	(method (set theClient theSeconds theMinutes theHours &tmp temp0 temp1 temp2)
		(= temp2 6)
		(if (== temp2 0) (= temp2 1))
		(= temp1 (/ (* theSeconds 60) temp2))
		(if (> argc 2)
			(= temp1 (+ temp1 (/ (* theMinutes 3600) temp2)))
		)
		(if (> argc 3)
			(= temp1 (+ temp1 (* (/ (* theHours 3600) temp2) 60)))
		)
		(= temp0 (if (& -info- $8000) (self new:) else self))
		(temp0 init: theClient cycleCnt: temp1)
		(return temp0)
	)
	
	(method (setCycle theCycler sendParams &tmp temp0)
		(= temp0 (if (& -info- $8000) (self new:) else self))
		(temp0 init: theCycler cycleCnt: sendParams)
		(return temp0)
	)
	
	;
	; .. function:: setReal(theClient theMilliseconds [theSeconds theMinutes])
	;
	; 	Sets the timer. This is more precise than set(), because you can specify milliseconds.
	;
	; 	:param heapPtr theClient: This object will have its cue() method called when the timer expires.
	; 	:param number theMilliseconds: The number of seconds for the timer.
	; 	:param number theSeconds: The number of seconds for the timer.
	; 	:param number theMinutes: The number of minutes for the timer (optional).
	;
	(method (setReal theClient theMilliseconds theSeconds theMinutes &tmp temp0 ms)
		(= ms theMilliseconds)
		(if (> argc 2) (= ms (+ ms (* theSeconds 60))))
		(if (> argc 3) (= ms (+ ms (* theMinutes 3600))))
		(= temp0 (if (& -info- $8000) (self new:) else self))
		(temp0 init: theClient seconds: ms)
		(return temp0)
	)
	
	(method (delete)
		(if (== client 0)
			(gTimers delete: self)
			(super dispose:)
		)
	)
	
	(method (setTicks param1 param2 &tmp temp0)
		(= temp0 (if (& -info- $8000) (self new:) else self))
		(temp0 ticks: (+ gGameTime param1) init: param2)
		(return temp0)
	)
)

;	
;	 TimeOut is simply a class that counts down a time.
(class TimeOut of Object
	(properties
		name {TO}
		timeLeft 0
	)
	
	(method (doit)
		(if timeLeft (-- timeLeft))
	)
	
	(method (set newTime)
		(= timeLeft newTime)
	)
)
