;;; Sierra Script 1.0 - (do not remove this comment)
(script# 929)
(include sci.sh)
(use Main)
(use Timer)
(use Cycle)
(use System)


;	
;	 This class is involved with lip-syncing and is used internally by and :class:`MouthSync`.
(class Sync of Object
	(properties
		syncTime -1
		syncCue $ffff
		prevCue $ffff
		syncNum -1
	)
	
	(method (syncStart modNum noun verb case seq)
		(DoSync syncSTART self modNum noun verb case seq)
		(if (!= syncCue -1) (= prevCue syncCue) (= syncTime 0))
	)
	
	(method (syncCheck)
		(if
			(and
				(!= syncCue -1)
				(or
					(u<= syncTime gSyncBias)
					(<= syncTime (DoAudio audPOSITION))
				)
			)
			(if (== (& $fff0 syncCue) 0)
				(= prevCue (| (& prevCue $fff0) syncCue))
			else
				(= prevCue syncCue)
			)
			(DoSync syncNEXT self)
		)
	)
	
	(method (syncStop)
		(= prevCue -1)
		(DoSync syncSTOP)
	)
)

;	
;	 This is a cycling class used by :class:`Talker`. It is driven off lip-sync information, and chooses
;	 the correct mouth cel for the Talker.
(class MouthSync of Cycle
	(properties
		client 0
		caller 0
		cycleDir 1
		cycleCnt 0
		completed 0
	)
	
	(method (init theClient modNum noun verb case seq)
		(super init: theClient)
		(if (IsObject gTheSync) (gTheSync syncStop: dispose:))
		(= gTheSync (Sync new:))
		(gTheSync syncStart: modNum noun verb case seq)
	)
	
	(method (doit &tmp temp0 gNewSyncSyncTime_2 gNewSyncSyncTime temp3)
		(super doit:)
		(if (!= (gTheSync prevCue?) -1)
			(= gNewSyncSyncTime (gTheSync syncTime?))
			(= temp3 0)
			(repeat
				(= gNewSyncSyncTime_2 (gTheSync syncTime?))
				(gTheSync syncCheck:)
				(breakif (== gNewSyncSyncTime_2 (gTheSync syncTime?)))
			)
			(if
				(and
					(!= gNewSyncSyncTime (gTheSync syncTime?))
					(!=
						(client cel?)
						(= temp0 (& $000f (gTheSync prevCue?)))
					)
				)
				(client cel: temp0)
			)
		else
			(= completed 1)
			(self cycleDone:)
		)
	)
	
	(method (dispose)
		(super dispose:)
		(if gTheSync (gTheSync dispose:) (= gTheSync 0))
	)
	
	(method (cue)
		(if gTheSync
			(gTheSync syncStop: dispose:)
			(= gTheSync 0)
			(if caller (caller cue:) (= caller 0))
		)
	)
)
