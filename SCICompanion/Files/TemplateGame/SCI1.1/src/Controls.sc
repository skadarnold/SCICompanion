;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 This script has a number of important general purpose classes and public procedures, mostly involving GUI elements.
(script# 255)
(include sci.sh)
(use Main)
(use Print)
(use System)

(public
	MouseStillDown 0
	GetNumber 1
)

;	
;	 Determines if there is a mouse release event.
;	
;	 :returns: FALSE if there is a mouse release event, TRUE otherwise.
(procedure (MouseStillDown &tmp newEvent temp1)
	(= newEvent (Event new:))
	(= temp1 (!= (newEvent type?) evMOUSERELEASE))
	(newEvent dispose:)
	(return temp1)
)

;	
;	 Shows an edit box and gets a number from the user.
;	
;	 .. function:: GetNumber(title [defaultNumber])
;	
;	 	:param string title: A title for the dialog.
;	 	:param defaultNumber: An optional default number.
;	
;	 	:returns: A number, or -1 if the user cancelled.
(procedure (GetNumber title defaultNumber &tmp [temp0 40])
	(= temp0 0)
	(if (> argc 1) (Format @temp0 {%d} defaultNumber))
	(return
		(if (GetInput @temp0 5 title)
			(ReadNumber @temp0)
		else
			-1
		)
	)
)

; A common base class for GUI controls.
(class Control of Object
	(properties
		type 0
		state $0000
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		key 0           ; The keyboard key associated with this control.
		said 0
		value 0         ; Arbitrary value associated with this control.
	)
	
	(method (doit)
		(return value)
	)
	
	(method (enable fEnable)
		(if fEnable
			(= state (| state csENABLED))
		else
			(= state (& state (~ csENABLED)))
		)
	)
	
	(method (select fSelect)
		(if fSelect
			(= state (| state csSELECTED))
		else
			(= state (& state (~ csSELECTED)))
		)
		(self draw:)
	)
	
	(method (handleEvent pEvent &tmp temp0 pEventType temp2)
		(if (pEvent claimed?) (return 0))
		(= temp0 0)
		(if
			(and
				(& state csENABLED)
				(or
					(and
						(== (= pEventType (pEvent type?)) 4)
						(== (pEvent message?) key)
					)
					(and (== pEventType evMOUSEBUTTON) (self check: pEvent))
				)
			)
			(pEvent claimed: TRUE)
			(= temp0 (self track: pEvent))
		)
		(return temp0)
	)
	
	; Returns TRUE if the x and y of the event lie inside the control.
	(method (check pEvent)
		(return
			(if
				(and
					(>= (pEvent x?) nsLeft)
					(>= (pEvent y?) nsTop)
					(< (pEvent x?) nsRight)
				)
				(< (pEvent y?) nsBottom)
			else
				0
			)
		)
	)
	
	; Highlights the control if the mouse is over it.
	(method (track pEvent &tmp temp0 temp1)
		(return
			(if (== evMOUSEBUTTON (pEvent type?))
				(= temp1 0)
				(repeat
					(= pEvent (Event new: evPEEK))
					(pEvent localize:)
					(= temp0 (self check: pEvent))
					(if (!= temp0 temp1)
						(HiliteControl self)
						(= temp1 temp0)
					)
					(pEvent dispose:)
					(breakif (not (MouseStillDown)))
				)
				(if temp0 (HiliteControl self))
				(return temp0)
			else
				(return self)
			)
		)
	)
	
	(method (setSize)
	)
	
	(method (move param1 param2)
		(= nsRight (+ nsRight param1))
		(= nsLeft (+ nsLeft param1))
		(= nsTop (+ nsTop param2))
		(= nsBottom (+ nsBottom param2))
	)
	
	(method (moveTo param1 param2)
		(self move: (- param1 nsLeft) (- param2 nsTop))
	)
	
	; Draws the control.
	(method (draw)
		(DrawControl self)
	)
	
	;	
	;	 :param number theType: ctlBUTTON, ctlTEXT, ctlEDIT, ctlICON or ctlSELETOR.
	;	 :returns: TRUE if this control is of the type specified.
	;	
	(method (isType theType)
		(return (== type theType))
	)
	
	;	
	;	 :param number stateFlags: Any combination of csENABLED, csEXIT, csFILTER or csSELECTED.
	;	 :returns: A non-zero value if the control has this state.
	;	
	(method (checkState stateFlags)
		(return (& state stateFlags))
	)
	
	(method (cycle)
	)
)

; A control that simply displays text.
(class DText of Control
	(properties
		type ctlTEXT
		state $0000
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		key 0           ; The keyboard key associated with this control.
		said 0
		value 0         ; Arbitrary value associated with this control.
		text 0          ; The text.
		font 1          ; The font.
		mode alLEFT     ; alLEFT, alCENTER or alRIGHT.
		rects 0
	)
	
	(method (new &tmp temp0)
		((super new:) font: gFont yourself:)
	)
	
	(method (dispose param1)
		(if (and text (or (not argc) (not param1)))
			(Memory memFREE (self text?))
		)
		(if rects (Memory memFREE (self rects?)))
		(super dispose:)
	)
	
	(method (handleEvent pEvent &tmp temp0 temp1 temp2 temp3 temp4)
		(asm
			lag      gTextCode
			bnt      code_0455
			pToa     rects
			bnt      code_0455
			pushi    3
			pushi    #type
			pushi    0
			lap      pEvent
			send     4
			push    
			pushi    1
			pushi    256
			calle    IsOneOf,  6
			bt       code_03b6
			pushi    #type
			pushi    0
			lap      pEvent
			send     4
			push    
			ldi      4
			eq?     
			bnt      code_0455
			pushi    #message
			pushi    0
			lap      pEvent
			send     4
			push    
			ldi      13
			eq?     
			bnt      code_0455
code_03b6:
			ldi      65535
			sat      temp0
			pushi    #globalize
			pushi    0
			pushi    73
			pushi    1
			pushi    1
			lap      pEvent
			send     10
code_03c6:
			pushi    2
			pTos     rects
			lst      temp0
			ldi      1
			add     
			push    
			calle    WordAt,  4
			push    
			ldi      $7777
			ne?     
			bnt      code_0455
			pushi    2
			pTos     rects
			+at      temp0
			push    
			calle    WordAt,  4
			sat      temp2
			pushi    2
			pTos     rects
			+at      temp0
			push    
			calle    WordAt,  4
			sat      temp1
			pushi    2
			pTos     rects
			+at      temp0
			push    
			calle    WordAt,  4
			sat      temp4
			pushi    2
			pTos     rects
			+at      temp0
			push    
			calle    WordAt,  4
			sat      temp3
			lst      temp2
			pushi    #x
			pushi    0
			lap      pEvent
			send     4
			le?     
			bnt      code_03c6
			pprev   
			lat      temp4
			le?     
			bnt      code_03c6
			lst      temp1
			pushi    #y
			pushi    0
			lap      pEvent
			send     4
			le?     
			bnt      code_03c6
			pprev   
			lat      temp3
			le?     
			bnt      code_03c6
			pushi    57
			pushi    #x
			lst      temp0
			ldi      4
			div     
			push    
			lag      gTextCode
			send     6
			pushi    #type
			pushi    1
			pushi    0
			pushi    73
			pushi    1
			pushi    0
			lap      pEvent
			send     12
			jmp      code_0455
			jmp      code_03c6
code_0455:
			pushi    #handleEvent
			pushi    1
			lsp      pEvent
			super    Control,  6
			ret     
		)
	)
	
	(method (setSize param1 &tmp [temp0 4])
		(TextSize @[temp0 0] text font (if argc param1 else 0))
		(= nsBottom (+ nsTop [temp0 2]))
		(= nsRight (+ nsLeft [temp0 3]))
	)
	
	(method (draw)
		(= rects (DrawControl self))
	)
)

(class Dialog of List
	(properties
		elements 0
		size 0
		text 0
		font 0
		window 0
		theItem 0
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		time 0
		caller 0
		seconds 0
		lastSeconds 0
		eatTheMice 0
		lastTicks 0
	)
	
	(method (doit param1 &tmp temp0 temp1 temp2)
		(= gGameTime (+ gTickOffset (GetTime)))
		(= temp2 0)
		(self eachElementDo: #init)
		(if theItem (theItem select: FALSE))
		(= theItem
			(if (and argc param1)
				param1
			else
				(self firstTrue: #checkState csENABLED)
			)
		)
		(if theItem (theItem select: TRUE))
		(if (not theItem)
			(= eatTheMice gEatTheMice)
			(= lastTicks (GetTime))
		else
			(= eatTheMice 0)
		)
		(= temp1 0)
		(while (not temp1)
			(= gGameTime (+ gTickOffset (GetTime)))
			(self eachElementDo: #cycle)
			(= temp0 ((Event new:) localize:))
			(if eatTheMice
				(-- eatTheMice)
				(if (== (temp0 type?) 1) (temp0 type: 0))
				(while (== lastTicks (GetTime))
				)
				(= lastTicks (GetTime))
			)
			(self eachElementDo: #perform checkHiliteCode self temp0)
			(= temp1 (self handleEvent: temp0))
			(temp0 dispose:)
			(breakif (self check:))
			(breakif (== temp1 -2))
			(Wait 1)
		)
		(return temp1)
	)
	
	(method (dispose &tmp theCaller)
		(self eachElementDo: #dispose release:)
		(if (== self gDialog)
			(SetPort gOldPort)
			(= gDialog 0)
			(= gOldPort NULL)
		)
		(if window (window dispose:) (= window 0))
		(= theItem 0)
		(= theCaller caller)
		(super dispose:)
		(if theCaller (theCaller cue:))
	)
	
	(method (open param1 param2)
		(if (and (PicNotValid) gCast)
			(Animate (gCast elements?) 0)
		)
		(= window (window new:))
		(window
			top: nsTop
			left: nsLeft
			bottom: nsBottom
			right: nsRight
			title: text
			type: param1
			priority: param2
			open:
		)
		(= seconds time)
		(self draw:)
	)
	
	(method (draw)
		(self eachElementDo: #draw)
	)
	
	(method (advance &tmp temp0 dialogFirst)
		(if theItem
			(theItem select: FALSE)
			(= dialogFirst (self contains: theItem))
			(repeat
				(= dialogFirst (self next: dialogFirst))
				(if (not dialogFirst) (= dialogFirst (self first:)))
				(= theItem (NodeValue dialogFirst))
				(breakif (& (theItem state?) csENABLED))
			)
			(theItem select: TRUE)
			(gGame
				setCursor:
					gCursorNumber
					1
					(+
						(theItem nsLeft?)
						(/ (- (theItem nsRight?) (theItem nsLeft?)) 2)
					)
					(- (theItem nsBottom?) 3)
			)
		)
	)
	
	(method (retreat &tmp temp0 dialogLast)
		(if theItem
			(theItem select: FALSE)
			(= dialogLast (self contains: theItem))
			(repeat
				(= dialogLast (self prev: dialogLast))
				(if (not dialogLast) (= dialogLast (self last:)))
				(= theItem (NodeValue dialogLast))
				(breakif (& (theItem state?) csENABLED))
			)
			(theItem select: TRUE)
			(gGame
				setCursor:
					gCursorNumber
					1
					(+
						(theItem nsLeft?)
						(/ (- (theItem nsRight?) (theItem nsLeft?)) 2)
					)
					(- (theItem nsBottom?) 3)
			)
		)
	)
	
	(method (move param1 param2)
		(= nsRight (+ nsRight param1))
		(= nsLeft (+ nsLeft param1))
		(= nsTop (+ nsTop param2))
		(= nsBottom (+ nsBottom param2))
	)
	
	(method (moveTo param1 param2)
		(self move: (- param1 nsLeft) (- param2 nsTop))
	)
	
	(method (center)
		(self
			moveTo:
				(+
					(window brLeft?)
					(/
						(-
							(- (window brRight?) (window brLeft?))
							(- nsRight nsLeft)
						)
						2
					)
				)
				(+
					(window brTop?)
					(/
						(-
							(- (window brBottom?) (window brTop?))
							(- nsBottom nsTop)
						)
						2
					)
				)
		)
	)
	
	(method (setSize &tmp dialogFirst temp1 [theNsTop 4])
		(if text
			(TextSize @[theNsTop 0] text font -1 0)
			(= nsTop [theNsTop 0])
			(= nsLeft [theNsTop 1])
			(= nsBottom [theNsTop 2])
			(= nsRight [theNsTop 3])
		else
			(= nsRight (= nsBottom (= nsLeft (= nsTop 0))))
		)
		(= dialogFirst (self first:))
		(while dialogFirst
			(= temp1 (NodeValue dialogFirst))
			(if (< (temp1 nsLeft?) nsLeft)
				(= nsLeft (temp1 nsLeft?))
			)
			(if (< (temp1 nsTop?) nsTop) (= nsTop (temp1 nsTop?)))
			(if (> (temp1 nsRight?) nsRight)
				(= nsRight (temp1 nsRight?))
			)
			(if (> (temp1 nsBottom?) nsBottom)
				(= nsBottom (temp1 nsBottom?))
			)
			(= dialogFirst (self next: dialogFirst))
		)
		(= nsRight (+ nsRight 4))
		(= nsBottom (+ nsBottom 4))
		(self moveTo: 0 0)
	)
	
	(method (handleEvent pEvent &tmp theTheItem pEventType pEventMessage)
		(if (& (pEvent type?) evJOYSTICK)
			(switch (pEvent message?)
				(JOY_DOWN
					(pEvent type: evKEYBOARD message: KEY_DOWN)
				)
				(JOY_UP
					(pEvent type: evKEYBOARD message: KEY_UP)
				)
				(JOY_LEFT
					(pEvent type: evKEYBOARD message: KEY_LEFT)
				)
				(JOY_RIGHT
					(pEvent type: evKEYBOARD message: KEY_RIGHT)
				)
			)
		)
		(= pEventType (pEvent type?))
		(= pEventMessage (pEvent message?))
		(= theTheItem (self firstTrue: #handleEvent pEvent))
		(if theTheItem
			(EditControl theItem 0)
			(if (not (theTheItem checkState: csEXIT))
				(if theItem (theItem select: FALSE))
				((= theItem theTheItem) select: TRUE)
				(theTheItem doit:)
				(= theTheItem 0)
			else
				(return theTheItem)
			)
		else
			(= pEventType (pEvent type?))
			(= pEventMessage (pEvent message?))
			(= theTheItem 0)
			(cond 
				(
					(and
						(or
							(== pEventType 256)
							(and
								(== pEventType evKEYBOARD)
								(== pEventMessage KEY_RETURN)
							)
						)
						theItem
						(theItem checkState: csENABLED)
					)
					(= theTheItem theItem)
					(EditControl theItem 0)
					(pEvent claimed: TRUE)
				)
				(
					(and
						(== pEventType evKEYBOARD)
						(== pEventMessage KEY_ESCAPE)
					)
					(pEvent claimed: TRUE)
					(= theTheItem -1)
				)
				(
					(and
						(not (self firstTrue: #checkState csENABLED))
						(or
							(and
								(== pEventType evKEYBOARD)
								(== pEventMessage KEY_RETURN)
							)
							(IsOneOf pEventType evMOUSEBUTTON 256)
						)
					)
					(pEvent claimed: TRUE)
					(= theTheItem -2)
				)
				(
					(and
						(IsObject theItem)
						(theItem isType: ctlEDIT)
						(== pEventType evKEYBOARD)
						(== pEventMessage KEY_RIGHT)
					)
					(if
					(>= (theItem cursor?) (StrLen (theItem text?)))
						(self advance:)
					else
						(EditControl theItem pEvent)
					)
				)
				(
					(and
						(IsObject theItem)
						(theItem isType: ctlEDIT)
						(== pEventType evKEYBOARD)
						(== pEventMessage KEY_NUMPAD4)
					)
					(if (<= (theItem cursor?) 0)
						(self retreat:)
					else
						(EditControl theItem pEvent)
					)
				)
				(
					(and
						(== pEventType evKEYBOARD)
						(IsOneOf pEventMessage 9 19712 20480)
					)
					(pEvent claimed: TRUE)
					(self advance:)
				)
				(
					(and
						(== pEventType evKEYBOARD)
						(IsOneOf pEventMessage 3840 19200 18432)
					)
					(pEvent claimed: TRUE)
					(self retreat:)
				)
				(else (EditControl theItem pEvent))
			)
		)
		(return theTheItem)
	)
	
	(method (check &tmp theLastSeconds)
		(return
			(if
				(and
					seconds
					(!=
						lastSeconds
						(= theLastSeconds (GetTime gtTIME_OF_DAY))
					)
				)
				(= lastSeconds theLastSeconds)
				(return (not (-- seconds)))
			else
				0
			)
		)
	)
)

(instance checkHiliteCode of Code
	(properties)
	
	(method (doit theControl theDialog pEvent)
		(if
			(and
				(& (theControl state?) csENABLED)
				(theControl check: pEvent)
				(not (& (theControl state?) csSELECTED))
			)
			((theDialog theItem?) select: FALSE)
			(theDialog theItem: theControl)
			(theControl select: TRUE)
		)
	)
)
