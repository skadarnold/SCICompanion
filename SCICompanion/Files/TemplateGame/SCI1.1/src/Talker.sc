;;; Sierra Script 1.0 - (do not remove this comment)
(script# 928)
(include sci.sh)
(use Main)
(use Print)
(use Sync)
(use RandCycle)
(use Cycle)
(use Actor)
(use System)


;	
;	 Blink is a cycler used with :class:`Talker` to make eyes blink randomly.
(class Blink of Cycle
	(properties
		client 0
		caller 0
		cycleDir 1
		cycleCnt 0
		completed 0
		waitCount 0
		lastCount 0
		waitMin 0
		waitMax 0
	)
	
	(method (init theClient averageWaitTicks)
		(if argc
			(= waitMin (/ averageWaitTicks 2))
			(= waitMax (+ averageWaitTicks waitMin))
			(super init: theClient)
		else
			(super init:)
		)
	)
	
	(method (doit &tmp blinkNextCel)
		(if waitCount
			(if (> (- gGameTime waitCount) 0)
				(= waitCount 0)
				(self init:)
			)
		else
			(= blinkNextCel (self nextCel:))
			(if
				(or
					(> blinkNextCel (client lastCel:))
					(< blinkNextCel 0)
				)
				(= cycleDir (- cycleDir))
				(self cycleDone:)
			else
				(client cel: blinkNextCel)
			)
		)
	)
	
	(method (cycleDone)
		(if (== cycleDir -1)
			(self init:)
		else
			(= waitCount (+ (Random waitMin waitMax) gGameTime))
		)
	)
)

;	
;	 Narrator is responsible for displaying messages and message sequences, and controlling the look
;	 of the text dialogs. A default Narrator
;	 (templateNarrator in main.sc) is defined as part of the template game, but you can create your
;	 own Narrator instances to define different text styles or colors for messages.
;	
;	 If you add a new Narrator instance, you must assign it some talker number, and then add code in the
;	 findTalker method of testMessage in Main.sc to direct that talker number to the right script and export.
;	
;	 See :doc:`/talkers` for more information Talkers.	
(class Narrator of Prop
	(properties
		x -1                ; -1 means center
		y -1                ; -1 means center
		z 0
		heading 0
		noun 0
		_case 0
		modNum -1
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE
		state $0000
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
		signal $0000
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
		caller 0
		disposeWhenDone 2
		ticks 0
		talkWidth 0
		keepWindow 0
		modeless 0
		font 0
		cueVal 0
		initialized 0
		showTitle 0
		color 0
		back 7
		curVolume 0
		saveCursor 0
	)
	
	(method (init &tmp theCurVolume_2 temp1 theCurVolume)
		(if
			(or
				(and (& gMessageType $0002) (not modeless))
				(not (HaveMouse))
			)
			(= saveCursor gCursorNumber)
			(gGame setCursor: gWaitCursor 1)
		)
		(= gGameTime (+ gTickOffset (GetTime)))
		(= initialized 1)
	)
	
	(method (doit)
		(if (and (!= ticks -1) (> (- gGameTime ticks) 0))
			(if
				(and
					(if (& gMessageType $0002)
						(== (DoAudio audPOSITION) -1)
					else
						1
					)
					(or (not keepWindow) (& gMessageType $0002))
				)
				(self dispose: disposeWhenDone)
				(return 0)
			)
		)
		(return 1)
	)
	
	(method (dispose theDisposeWhenDone)
		(= ticks -1)
		(if (or (not argc) (== theDisposeWhenDone 1))
			(cond 
				(modeless
					(gOldKH delete: self)
					(gOldMH delete: self)
					(gTheDoits delete: self)
				)
				((and gFastCast (gFastCast contains: self))
					(gFastCast delete: self)
					(if (gFastCast isEmpty:)
						(gFastCast dispose:)
						(= gFastCast 0)
					)
				)
			)
			(if (& gMessageType $0002) (DoAudio audSTOP))
			(= modNum -1)
			(= initialized 0)
		)
		(if gDialog (gDialog dispose:))
		(if saveCursor
			(if
				(or
					(and (& gMessageType $0002) (not modeless))
					(not (HaveMouse))
				)
				(gGame setCursor: saveCursor)
			)
		else
			(= saveCursor 0)
		)
		(if caller (caller cue: cueVal))
		(= cueVal 0)
		(DisposeClone self)
	)
	
	(method (handleEvent pEvent)
		(return
			(cond 
				((pEvent claimed?))
				((== ticks -1) (return 0))
				(else
					(if (not cueVal)
						(switch (pEvent type?)
							(256 (= cueVal 0))
							(evMOUSEBUTTON
								(= cueVal (& (pEvent modifiers?) emSHIFT))
							)
							(evKEYBOARD
								(= cueVal (== (pEvent message?) KEY_ESCAPE))
							)
						)
					)
					(if
						(or
							(& (pEvent type?) $4101)
							(and
								(& (pEvent type?) evKEYBOARD)
								(IsOneOf (pEvent message?) KEY_RETURN KEY_ESCAPE)
							)
						)
						(pEvent claimed: TRUE)
						(self dispose: disposeWhenDone)
					)
				)
			)
		)
	)
	
	(method (say buffer theCaller)
		(if gIconBar (gIconBar disable:))
		(if (not initialized) (self init:))
		(= caller
			(if (and (> argc 1) theCaller) theCaller else 0)
		)
		(if (& gMessageType $0001) (self startText: buffer))
		(if (& gMessageType $0002) (self startAudio: buffer))
		(cond 
			(modeless
				(gOldMH addToFront: self)
				(gOldKH addToFront: self)
				(gTheDoits add: self)
			)
			((IsObject gFastCast) (gFastCast add: self))
			(else
				(= gFastCast (EventHandler new:))
				(gFastCast name: {fastCast} add: self)
			)
		)
		(= ticks (+ ticks 60 gGameTime))
		(return 1)
	)
	
	(method (startText buffer &tmp temp0)
		; No need to check this. If we did the check, then if there's no audio, the ticks would be 0
		; startAudio is always called after this, and it sets ticks.
		; (if (not & gMessageType $0002)
		(= temp0 (StrLen buffer))
		(= ticks (Max 240 (* gTextReadSpeed 2 temp0)))
		; )
		(if gDialog (gDialog dispose:))
		(self display: buffer)
		(return temp0)
	)
	
	(method (display theText &tmp theTalkWidth newGSq5Win [textBuffer 500])
		(if (> (+ x talkWidth) 318)
			(= theTalkWidth (- 318 x))
		else
			(= theTalkWidth talkWidth)
		)
		(= newGSq5Win (gWindow new:))
		(newGSq5Win color: color back: back)
		(if showTitle (Print addTitle: name))
		(Print
			window: newGSq5Win
			posn: x y
			font: font
			width: theTalkWidth
			modeless: 1
		)
		(if (& gMessageType $0002)
			(Message
				msgGET
				(WordAt theText 0)
				(WordAt theText 1)
				(WordAt theText 2)
				(WordAt theText 3)
				(WordAt theText 4)
				@textBuffer
			)
			(Print addText: @textBuffer)
		else
			(Print addText: theText)
		)
		(Print init:)
	)
	
	(method (startAudio buffer &tmp temp0 temp1 temp2 temp3 temp4)
		(= temp0 (WordAt buffer 0))
		(= temp1 (WordAt buffer 1))
		(= temp2 (WordAt buffer 2))
		(= temp3 (WordAt buffer 3))
		(= temp4 (WordAt buffer 4))
		(if
		(ResCheck rsAUDIO36 temp0 temp1 temp2 temp3 temp4)
			(= ticks (DoAudio audPLAY temp0 temp1 temp2 temp3 temp4))
		)
	)
)

;	
;	 Talker is similar to :class:`Narrator`, but includes support for showing a bust, eyes and mouth
;	 while the messages are shown or spoken.
;	 The mouth can also be driven by lip-sync data, if present.
;	
;	 If you add a new Talker instance, you must assign it some talker number, and then add code in the
;	 findTalker method of testMessage in Main.sc to direct that talker number to the right script and export.
;	
;	 See :doc:`/talkers` for more information Talkers.
(class Talker of Narrator
	(properties
		x -1
		y -1
		z 0
		heading 0
		noun 0
		_case 0
		modNum -1
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE
		state $0000
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
		signal $0000
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
		caller 0
		disposeWhenDone 2
		ticks 0
		talkWidth 318
		keepWindow 0
		modeless 0
		font 0
		cueVal 0
		initialized 0
		showTitle 0
		color 0
		back 7
		curVolume 0
		saveCursor 0
		bust 0
		eyes 0
		mouth 0
		viewInPrint 0
		textX 0
		textY 0
		useFrame 0
		blinkSpeed 100
	)
	
	;
	; .. function:: init([theBust theEyes theMouth])
	;
	; 	Override this method in your Talker instance, and call (super:init(bust eyes mouth)) with
	; 	the appropriate Props for theBust, theEyes and theMouth. You will need to
	; 	position them at the correct spot.
	;
	; 	:param heapPtr theBust: A :class:`Prop` instance for the overall face (bust) of the talker.
	; 	:param heapPtr theEyes: A :class:`Prop` instance for the eyes.
	; 	:param heapPtr theMouth: A :class:`Prop` instance for the talker's mouth.
	;
	(method (init theBust theEyes theMouth)
		(if argc
			(= bust theBust)
			(if (> argc 1)
				(= eyes theEyes)
				(if (> argc 2) (= mouth theMouth))
			)
		)
		(self setSize:)
		(super init:)
	)
	
	(method (doit)
		(if (and (super doit:) mouth) (self cycle: mouth))
		(if eyes (self cycle: eyes))
	)
	
	(method (dispose param1)
		(if (and mouth underBits)
			(mouth cel: 0)
			(DrawCel
				(mouth view?)
				(mouth loop?)
				0
				(+ (mouth nsLeft?) nsLeft)
				(+ (mouth nsTop?) nsTop)
				-1
			)
		)
		(if (and mouth (mouth cycler?))
			(if ((mouth cycler?) respondsTo: #cue)
				((mouth cycler?) cue:)
			)
			(mouth setCycle: 0)
		)
		(if (or (not argc) (== param1 1))
			(if (and eyes underBits)
				(eyes setCycle: 0 cel: 0)
				(DrawCel
					(eyes view?)
					(eyes loop?)
					0
					(+ (eyes nsLeft?) nsLeft)
					(+ (eyes nsTop?) nsTop)
					-1
				)
			)
			(self hide:)
		)
		(super dispose: param1)
	)
	
	(method (hide)
		(Graph grRESTORE_BOX underBits)
		(= underBits 0)
		(Graph grREDRAW_BOX nsTop nsLeft nsBottom nsRight)
		(if gIconBar (gIconBar enable:))
	)
	
	(method (show &tmp temp0)
		(if (not underBits)
			(= underBits
				(Graph grSAVE_BOX nsTop nsLeft nsBottom nsRight 1)
			)
		)
		(= temp0 (PicNotValid))
		(PicNotValid 1)
		(if bust
			(DrawCel
				(bust view?)
				(bust loop?)
				(bust cel?)
				(+ (bust nsLeft?) nsLeft)
				(+ (bust nsTop?) nsTop)
				-1
			)
		)
		(if eyes
			(DrawCel
				(eyes view?)
				(eyes loop?)
				(eyes cel?)
				(+ (eyes nsLeft?) nsLeft)
				(+ (eyes nsTop?) nsTop)
				-1
			)
		)
		(if mouth
			(DrawCel
				(mouth view?)
				(mouth loop?)
				(mouth cel?)
				(+ (mouth nsLeft?) nsLeft)
				(+ (mouth nsTop?) nsTop)
				-1
			)
		)
		(DrawCel view loop cel nsLeft nsTop -1)
		(Graph grUPDATE_BOX nsTop nsLeft nsBottom nsRight 1)
		(PicNotValid temp0)
	)
	
	(method (say)
		(if (and (> view 0) (not underBits)) (self init:))
		(super say: &rest)
	)
	
	(method (startText &tmp temp0)
		(if (not viewInPrint) (self show:))
		(= temp0 (super startText: &rest))
		(if mouth (mouth setCycle: RandCycle (* 4 temp0) 0 1))
		(if (and eyes (not (eyes cycler?)))
			(eyes setCycle: Blink blinkSpeed)
		)
	)
	
	(method (display theText &tmp temp0 theTalkWidth temp2 newGSq5Win [textBuffer 500])
		(= newGSq5Win (gWindow new:))
		(newGSq5Win color: color back: back)
		(if viewInPrint
			(= temp0 (if useFrame loop else (bust loop?)))
			(if showTitle (Print addTitle: name))
			(Print
				window: newGSq5Win
				posn: x y
				modeless: 1
				font: font
				addText: theText
				addIcon: view temp0 cel 0 0
				init:
			)
		else
			(if (not (+ textX textY))
				(= textX (+ (- nsRight nsLeft) 5))
			)
			(= temp2 (+ nsLeft textX))
			(if (> (+ temp2 talkWidth) 318)
				(= theTalkWidth (- 318 temp2))
			else
				(= theTalkWidth talkWidth)
			)
			(if showTitle (Print addTitle: name))
			(Print
				window: newGSq5Win
				posn: (+ x textX) (+ y textY)
				modeless: 1
				font: font
				width: theTalkWidth
			)
			(if (& gMessageType $0002)
				(Message
					msgGET
					(WordAt theText 0)
					(WordAt theText 1)
					(WordAt theText 2)
					(WordAt theText 3)
					(WordAt theText 4)
					@textBuffer
				)
				(Print addText: @textBuffer)
			else
				(Print addText: theText)
			)
			(Print init:)
		)
	)
	
	(method (startAudio buffer &tmp temp0 temp1 temp2 temp3 temp4 temp5)
		(self show:)
		(if mouth
			(= temp0 (WordAt buffer 0))
			(= temp1 (WordAt buffer 1))
			(= temp2 (WordAt buffer 2))
			(= temp3 (WordAt buffer 3))
			(= temp4 (WordAt buffer 4))
			(if
			(ResCheck rsSYNC36 temp0 temp1 temp2 temp3 temp4)
				(mouth setCycle: MouthSync temp0 temp1 temp2 temp3 temp4)
				(= temp5 (super startAudio: buffer))
			else
				(= temp5 (super startAudio: buffer))
				(mouth setCycle: RandCycle temp5 0)
			)
		else
			(= temp5 (super startAudio: buffer))
		)
		(if (and eyes (not (eyes cycler?)))
			(eyes setCycle: Blink blinkSpeed)
		)
	)
	
	(method (cycle theProp &tmp temp0 [temp1 100])
		(if (and theProp (theProp cycler?))
			(= temp0 (theProp cel?))
			((theProp cycler?) doit:)
			(if (!= temp0 (theProp cel?))
				(DrawCel
					(theProp view?)
					(theProp loop?)
					(theProp cel?)
					(+ (theProp nsLeft?) nsLeft)
					(+ (theProp nsTop?) nsTop)
					-1
				)
				(theProp
					nsRight:
						(+
							(theProp nsLeft?)
							(CelWide
								(theProp view?)
								(theProp loop?)
								(theProp cel?)
							)
						)
				)
				(theProp
					nsBottom:
						(+
							(theProp nsTop?)
							(CelHigh
								(theProp view?)
								(theProp loop?)
								(theProp cel?)
							)
						)
				)
				(Graph
					grUPDATE_BOX
					(+ (theProp nsTop?) nsTop)
					(+ (theProp nsLeft?) nsLeft)
					(+ (theProp nsBottom?) nsTop)
					(+ (theProp nsRight?) nsLeft)
					1
				)
			)
		)
	)
	
	(method (setSize)
		(= nsLeft x)
		(= nsTop y)
		(= nsRight
			(+
				nsLeft
				(Max
					(if view (CelWide view loop cel) else 0)
					(if (IsObject bust)
						(+
							(bust nsLeft?)
							(CelWide (bust view?) (bust loop?) (bust cel?))
						)
					else
						0
					)
					(if (IsObject eyes)
						(+
							(eyes nsLeft?)
							(CelWide (eyes view?) (eyes loop?) (eyes cel?))
						)
					else
						0
					)
					(if (IsObject mouth)
						(+
							(mouth nsLeft?)
							(CelWide (mouth view?) (mouth loop?) (mouth cel?))
						)
					else
						0
					)
				)
			)
		)
		(= nsBottom
			(+
				nsTop
				(Max
					(if view (CelHigh view loop cel) else 0)
					(if (IsObject bust)
						(+
							(bust nsTop?)
							(CelHigh (bust view?) (bust loop?) (bust cel?))
						)
					else
						0
					)
					(if (IsObject eyes)
						(+
							(eyes nsTop?)
							(CelHigh (eyes view?) (eyes loop?) (eyes cel?))
						)
					else
						0
					)
					(if (IsObject mouth)
						(+
							(mouth nsTop?)
							(CelHigh (mouth view?) (mouth loop?) (mouth cel?))
						)
					else
						0
					)
				)
			)
		)
	)
)
