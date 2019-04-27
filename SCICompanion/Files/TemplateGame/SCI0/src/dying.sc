;;; Sierra Script 1.0 - (do not remove this comment)
;
; SCI Template Game
; By Brian Provinciano
; ******************************************************************************
; dying.sc
; Contains a public script instance to handle when the ego dies.
(script# DYING_SCRIPT)
(include sci.sh)
(include game.sh)
(use main)
(use controls)
(use dcicon)
(use cycle)
(use obj)

(public
	DyingScript 0
)




(instance DyingScript of Script
	(properties)
	
	(method (changeState newState &tmp mbResult message)
		(= state newState)
		(cond 
			((== state 0)
				(ProgramControl)
				(gTheMusic fade:)
				(gRoom setScript: 0)
				(Load rsSOUND 2)
				(= seconds 3)
			)
			((== state 1)
				(gTheSoundFX stop:)
				(gTheMusic number: 2 loop: 1 priority: -1 play:)
				; The following lines give a typical Sierra style
				; message box telling the player that they have died.
				; You can customize it to your liking.
				(if (!= NULL caller)
					(Load rsVIEW caller)
					(deadIcon view: caller)
				else
					(Load rsVIEW DYING_SCRIPT)
					(deadIcon view: DYING_SCRIPT)
				)
				(if (!= NULL register)
					(= message register)
				else
					(= message {You are dead.})
				)
				(if
					(Print
						message
						#font
						gDeadFont
						#icon
						deadIcon
						#button
						{Keep On Muddling}
						0
						#button
						{Order A Hintbook}
						1
					)
					(Print
						{Order a hint book? Who do you think I am? Sierra On-Line? Naw, I'm just a measly computer programmer that made this game in his basement!}
					)
				)
				(repeat
					(= mbResult
						(Print
							{Remember:\nsave early, save often!}
							#title
							{Brian Provinciano says:}
							#font
							gDeadFont
							#button
							{Restore}
							1
							#button
							{Restart}
							2
							#button
							{__Quit__}
							3
						)
					)
					(switch mbResult
						(1
							(if (!= (gGame restore:) -1) (return))
						)
						(2 (gGame restart:) (return))
						(3 (= gQuitGame TRUE) (return))
					)
				)
			)
		)
	)
)


(instance deadIcon of DCIcon
	(properties)
	
	(method (init)
		(super init:)
		(if (== gRoomNumberExit 540)
			(= cycler (End new:))
			(cycler init: self)
		)
	)
)
