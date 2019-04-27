;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 This is a room for handling death scenes. In the sGeneric Script, you can switch based on of the
;	 gDeathReason global variable to display custom scenes depending on the type of death.
;	 The death room is triggered by calling the :func:`Die` procedure with a number corresponding to a death reason.
(script# DEATH_SCRIPT)
(include sci.sh)
(include Verbs.sh)
(include game.sh)
(include 20.shm)
(use Main)
(use Cycle)
(use Game)
(use Actor)
(use System)

(public
	deathRoom 0
)

(local
	[messageBuffer 200]
)
(procedure (localproc_006e)
	(iWannaQuit init:)
	(iWannaRestore init:)
	(iWannaRestart init:)
	(gUser canControl: 1 canInput: 1)
	(gIconBar enable:)
	(gIconBar enable: ICONINDEX_DO)
	(gIconBar select: (gIconBar at: 2))
	(gGame setCursor: 999)
)

(instance deathRoom of Room
	(properties
		picture 200
	)
	
	(method (init)
		(Palette palSET_INTENSITY 0 255 100)
		(PalVary pvUNINIT)
		(gGame handsOff:)
		(super init:)
		(gMusic2 stop:)
		(switch gDeathReason
			; TODO: Add special cases here
			(else 
				(gRoom setScript: sGeneric)
			)
		)
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(V_DO)
			(else 
				(super doVerb: theVerb &rest)
			)
		)
	)
)

(instance sGeneric of Script
	(properties)
	
	(method (changeState newState)
		(switch (= state newState)
			(0
				(gGame handsOff:)
				(Message
					msgGET
					DEATH_SCRIPT
					N_DEATH
					0
					0
					gDeathReason
					@messageBuffer
				)
				(Display
					@messageBuffer
					dsCOORD
					143
					68
					dsCOLOR
					0
					dsBACKGROUND
					5
					dsFONT
					1605
					dsWIDTH
					140
					dsALIGN
					alCENTER
				)
				(skull
					view: 2000
					loop: 0
					cel: 0
					init:
					posn: 42 101
					setCycle: Forward
				)
				(= seconds 2)
			)
			(1
				(localproc_006e)
				(self dispose:)
			)
		)
	)
)

(instance skull of Prop
	(properties
		signal $4000
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(V_DO)
			(else 
				(super doVerb: theVerb &rest)
			)
		)
	)
)

(instance iWannaRestart of View
	(properties
		x 50
		y 170
		view 2099
		loop 1
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(V_DO
				(self cel: 1)
				(gGame restart:)
			)
			(else 
				(super doVerb: theVerb &rest)
			)
		)
	)
)

(instance iWannaRestore of View
	(properties
		x 150
		y 170
		view 2099
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(V_DO
				(self cel: 1)
				(gGame restore:)
			)
			(else 
				(super doVerb: theVerb &rest)
			)
		)
	)
)

(instance iWannaQuit of View
	(properties
		x 250
		y 170
		view 2099
		loop 2
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(V_DO
				(self cel: 1)
				(= gQuitGame TRUE)
			)
			(else 
				(super doVerb: theVerb &rest)
			)
		)
	)
)
