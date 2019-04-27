;;; Sierra Script 1.0 - (do not remove this comment)
(script# 977)
(include sci.sh)
(use Main)
(use StopWalk)
(use Sight)
(use Cycle)
(use System)


(local
	[local0 8] = [2 6 4 0 3 5 1 7]
	[local8 8] = [3 6 0 4 2 5 1 7]
)
;	
;	 This code is responsible for determining the correct view loop to use
;	 based on an object's heading. It works for 4 and 8 loop actors.
(class GradualLooper of Code
	(properties
		name {Grooper}
		client 0
		oldCycler 0
		oldMover 0
		caller 0
	)
	
	(method (doit theClient theAngle theCaller param4 &tmp temp0 temp1)
		(if (not client) (= client theClient))
		(if (>= argc 3) (= caller theCaller))
		(if (& (client signal?) $0800)
			(if caller (caller cue:))
			(= caller 0)
			(return)
		)
		(= temp1 (if (< (NumLoops client) 8) 4 else 8))
		(if
			(or
				(not (gCast contains: client))
				(and (>= argc 4) param4)
			)
			(client
				loop:
					[local8
					(*
						(if (== temp1 4) 2 else 1)
						(/
							(UModulo (+ (client heading?) (/ 180 temp1)) 360)
							(/ 360 temp1)
						)
					)]
			)
			(if caller (caller cue:))
			(= caller 0)
			(return)
		)
		(= temp0
			(if
				(and
					(== (client loop?) (- (NumLoops client) 1))
					((client cycler?) isKindOf: StopWalk)
					(== ((client cycler?) vStopped?) -1)
				)
				[local0
				(client cel?)]
			else
				[local0
				(client loop?)]
			)
		)
		(if oldMover (oldMover dispose:) (= oldMover 0))
		(if
			(and
				(IsObject oldCycler)
				(or
					(oldCycler isMemberOf: GradualCycler)
					(not ((client cycler?) isMemberOf: GradualCycler))
				)
			)
			(oldCycler dispose:)
			(= oldCycler 0)
		)
		(if (not oldCycler) (= oldCycler (client cycler?)))
		(if
			(and
				(client cycler?)
				((client cycler?) isMemberOf: GradualCycler)
			)
			((client cycler?) dispose:)
		)
		(= oldMover (client mover?))
		(client
			cycler: 0
			mover: 0
			setMotion: 0
			setCycle: GradualCycler self temp0
		)
	)
	
	(method (dispose)
		(if (IsObject oldCycler)
			(oldCycler dispose:)
			(= oldCycler 0)
		)
		(if (IsObject oldMover)
			(oldMover dispose:)
			(= oldMover 0)
		)
		(if client (client looper: 0))
		(super dispose:)
	)
	
	(method (cue &tmp theCaller)
		(if (not (IsObject (client mover?)))
			(client mover: oldMover)
		)
		(if (IsObject oldCycler) (client cycler: oldCycler))
		(= theCaller caller)
		(= caller (= oldMover (= oldCycler 0)))
		(if theCaller (theCaller cue: &rest))
	)
)

;	
;	 This class is used internally by :class:`GradualLooper`.
(class GradualCycler of Cycle
	(properties
		name {Grycler}
		client 0
		caller 0
		cycleDir 1
		cycleCnt 0
		completed 0
		loopIndex 0
		numOfLoops 0
	)
	
	(method (init param1 theCaller theLoopIndex)
		(super init: param1)
		(= caller theCaller)
		(= numOfLoops (if (< (NumLoops client) 8) 4 else 8))
		(= cycleDir
			(-
				(Sign
					(AngleDiff (* theLoopIndex 45) (param1 heading?))
				)
			)
		)
		(= loopIndex theLoopIndex)
		(if (self loopIsCorrect:)
			(if
				(and
					(((client looper?) oldCycler?) isKindOf: StopWalk)
					(== (((client looper?) oldCycler?) vStopped?) -1)
				)
				(client loop: [local8 loopIndex])
			)
			(self cycleDone:)
		)
	)
	
	(method (doit)
		(client loop: (self nextCel:))
		(if (self loopIsCorrect:) (self cycleDone:))
	)
	
	(method (nextCel)
		(return
			(if
				(or
					(< (Abs (- gGameTime cycleCnt)) (client cycleSpeed?))
					(self loopIsCorrect:)
				)
				(client loop?)
			else
				(= cycleCnt gGameTime)
				(= loopIndex
					(+ loopIndex (* cycleDir (/ 8 numOfLoops)))
				)
				(= loopIndex (UModulo loopIndex 8))
				[local8
				loopIndex]
			)
		)
	)
	
	(method (cycleDone)
		(= gDoMotionCue (= completed TRUE))
	)
	
	(method (loopIsCorrect)
		(return
			(<
				(Abs (AngleDiff (* loopIndex 45) (client heading?)))
				(+ (/ 180 numOfLoops) 1)
			)
		)
	)
)
