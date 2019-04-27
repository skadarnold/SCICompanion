;;; Sierra Script 1.0 - (do not remove this comment)
(script# 18)
(include sci.sh)
(include Verbs.sh)
(include 0.shm)
(use Main)
(use Smopper)
(use Ego)
(use Cycle)
(use System)


;	
;	 GameEgo is a game-specific subclass of :class:`Ego`. Here, you can put default answers for
;	 looking, talking and performing actions on yourself.
(class GameEgo of Ego
	(properties
		x 0
		y 0
		z 0
		heading 0
		noun N_EGO
		_case 0
		modNum 0
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE
		state $0002
		approachX 0
		approachY 0
		approachDist 0
		_approachVerbs 0
		yStep 2
		view -1
		loop 0
		cel 0
		priority 0
		underBits 0
		signal $2000
		lsTop 0
		lsLeft 0
		lsBottom 0
		lsRight 0
		brTop 0
		brLeft 0
		brBottom 0
		brRight 0
		scaleSignal $0000
		scaleX 128
		scaleY 128
		maxScale 128
		cycleSpeed 6
		script 0
		cycler 0
		timer 0
		detailLevel 0
		scaler 0
		illegalBits $8000
		xLast 0
		yLast 0
		xStep 3
		origStep 770
		moveSpeed 6
		blocks 0
		baseSetter 0
		mover 0
		looper 0
		viewer 0
		avoider 0
		code 0
		edgeHit 0
	)
	
	(method (handleEvent pEvent &tmp temp0 temp1 temp2)
		(= temp1 (pEvent type?))
		(= temp2 (pEvent message?))
		(return
			(cond 
				((and script (script handleEvent: pEvent)) 1)
				((& temp1 $0040) (return 0))
				(else (super handleEvent: pEvent &rest))
			)
		)
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(V_LOOK
				(gMessager say: N_EGO V_LOOK 0 (Random 1 2) 0 0)
			)
			(V_DO
				(gMessager say: N_EGO V_DO 0 (Random 1 2) 0 0)
			)
			(V_TALK
				(gMessager say: N_EGO V_TALK 0 (Random 1 2) 0 0)
			)
			(else 
				(super doVerb: theVerb &rest)
			)
		)
	)
	
	(method (setSpeed newSpeed)
		(= gGEgoMoveSpeed (super setSpeed: newSpeed))
	)
)

;	
;	 FiddleStopWalk is a game-specific subclass of :class:`Smopper` (which allows separate views
;	 for starting, stopping and stopped animations). It extends Smopper by providing some random
;	 idle animations. The version of FiddleStopWalk in the SCI1.1 template
;	 game uses SQ5-specific views (so it won't work with the template game), but is included as an example of what you
;	 might want to implement in your game.
(class FiddleStopWalk of Smopper
	(properties
		client 0
		caller 0
		cycleDir 1
		cycleCnt 0
		completed 0
		vInMotion 0
		vStopped 0
		vSlow 0
		vStart 0
		stopState 0
		useSlow 0
		cSpeed 0
		oldSpeed 0
		newCel 0
		tempMover 0
		lCel 0
		ticks 1200
		curTicks -1
		lastTicks 0
		oldControl 0
		oldCycSpeed 0
	)
	
	(method (doit &tmp [temp0 2])
		(if (client isStopped:)
			(cond 
				((!= (gEgo loop?) 8)
					(if (IsOneOf vInMotion 0 1)
						(= oldCycSpeed gGEgoMoveSpeed)
						(cond 
							((== vInMotion 1) (= vSlow 3))
							((IsOneOf (gEgo loop?) 2 4 5) (= vSlow (if (Random 0 1) 12 else 2))) ; The loops that face the screen.
							(else (= vSlow 12))
						)
						(= curTicks ticks)
					else
						(= vSlow 0)
					)
					(super doit: &rest)
				)
				(
					(and
						(== (gEgo loop?) 8)
						(!= curTicks -1)
						(<=
							(= curTicks (- curTicks (Abs (- gGameTime lastTicks))))
							0
						)
					)
					(= curTicks -1)
					(super doit: &rest)
				)
				(
					(and
						(== curTicks -1)
						(not (gRoom script?))
						(not (gEgo script?))
						(gUser canControl:)
						(== (gEgo view?) 0)
						(== (gEgo loop?) (- (NumLoops gEgo) 1))
					)
					(= curTicks ticks)
					(= lCel (gEgo cel?))
					(= oldCycSpeed (gEgo cycleSpeed?))
					(if (IsOneOf (gEgo cel?) 2 4 5)
						(gEgo view: (if (Random 0 1) 10 else 2))
					else
						(gEgo view: 10)
					)
					(gEgo
						loop: lCel
						cel: 0
						cycleSpeed: 15
						setCycle: EndLoop self
					)
				)
				(else (super doit: &rest))
			)
		else
			(super doit: &rest)
		)
		(= lastTicks gGameTime)
	)
	
	(method (cue)
		(gEgo
			view: 0
			cel: lCel
			cycleSpeed: oldCycSpeed
			cycler: self
		)
		(gEgo loop: (- (NumLoops gEgo) 1))
		(self client: gEgo)
	)
)
