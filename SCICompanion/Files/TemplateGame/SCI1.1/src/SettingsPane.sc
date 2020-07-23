;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 This script contains the settings dialog and the controls that are used in the settings dialog.
(script# GAMECONTROLS_SCRIPT)
(include sci.sh)
(include game.sh)
(include 0.shm)
(use Main)
(use Print)
(use Slider)
(use BorderWindow)
(use IconItem)
(use GameControls)
(use System)

(public
	gameControlCode 0
	gcWin 1
)

(instance gameControls of GameControls
	(properties)
	
	(method (dispatchEvent param1 &tmp winEraseOnly temp1 temp2 [temp3 50] temp53 temp54)
		(= temp53 (param1 type?))
		(= temp54 (param1 message?))
		(return
			(cond 
				((& temp53 evHELP)
					(= temp1 (self firstTrue: #onMe param1))
					(if
						(and
							temp1
							((= temp1 (self firstTrue: #onMe param1)) helpVerb?)
						)
						(= temp2 (GetPort))
						(if (gWindow respondsTo: #eraseOnly)
							(= winEraseOnly (gWindow eraseOnly?))
							(gWindow eraseOnly: 1)
							(Print
								font: gFont
								width: 250
								addText: (temp1 noun?) (temp1 helpVerb?) 0 1 0 0 (temp1 modNum?)
								init:
							)
							(gWindow eraseOnly: winEraseOnly)
						else
							(Print
								font: gFont
								width: 250
								addText: (temp1 noun?) (temp1 helpVerb?) 0 1 0 0 (temp1 modNum?)
								init:
							)
						)
						(SetPort temp2)
					)
					(if helpIconItem
						(helpIconItem signal: (& (helpIconItem signal?) $ffef))
					)
					(gGame setCursor: 999)
					(return 0)
				)
				((& temp53 $0040)
					(switch temp54
						(5
							(cond 
								(
									(and
										(IsObject highlightedIcon)
										(highlightedIcon respondsTo: #retreat)
									)
									(highlightedIcon retreat:)
									(return 0)
								)
								(
									(or
										(not (IsObject highlightedIcon))
										(& (highlightedIcon signal?) $0100)
									)
									(self advance:)
									(return 0)
								)
							)
						)
						(1
							(cond 
								(
									(and
										(IsObject highlightedIcon)
										(highlightedIcon respondsTo: #advance)
									)
									(highlightedIcon advance:)
									(return 0)
								)
								(
									(or
										(not (IsObject highlightedIcon))
										(& (highlightedIcon signal?) $0100)
									)
									(self retreat:)
									(return 0)
								)
							)
						)
						(else 
							(super dispatchEvent: param1)
						)
					)
				)
				(else (super dispatchEvent: param1))
			)
		)
	)
)

(instance gameControlCode of Code
	(properties)
	
	(method (doit)
		(= gGameControls gameControls)
		(gGameControls
			add:
				detailSlider
				(volumeSlider
					theObj: gGame
					selector: #masterVolume
					yourself:
				)
				(speedSlider theObj: gEgo selector: #setSpeed yourself:)
				(iconSave theObj: gGame selector: #save yourself:)
				(iconRestore theObj: gGame selector: #restore yourself:)
				(iconRestart theObj: gGame selector: #restart yourself:)
				(iconQuit theObj: gGame selector: #quitGame yourself:)
				iconOk
				(iconAbout theObj: gGame selector: #showAbout yourself:)
				iconMessageMode
				iconHelp
			eachElementDo: #highlightColor 0
			eachElementDo: #lowlightColor 5
			eachElementDo: #modNum 0
			eachElementDo: #helpVerb V_HELP
			helpIconItem: iconHelp
			window: gcWin
			curIcon: iconSave
			state: 2048
			show:
		)
	)
)

(instance gcWin of BorderWindow
	(properties)
	
	(method (open &tmp temp0 [temp1 25] [temp26 25] temp51)
		(= temp51 -1)
		(self
			top: (/ (- 200 (+ (CelHigh 995 1 1) 6)) 2)
			left: (/ (- 320 (+ 151 (CelWide 995 0 1))) 2)
			bottom:
				(+
					(CelHigh 995 1 1)
					6
					(/ (- 200 (+ (CelHigh 995 1 1) 6)) 2)
				)
			right:
				(+
					151
					(CelWide 995 0 1)
					(/ (- 320 (+ 151 (CelWide 995 0 1))) 2)
				)
			priority: temp51
		)
		(super open:)
		(DrawCel
			995
			0
			5
			(+
				(/
					(-
						(- (+ 151 (CelWide 995 0 1)) (+ 4 (CelWide 995 1 1)))
						(CelWide 995 0 5)
					)
					2
				)
				4
				(CelWide 995 1 1)
			)
			3
			temp51
		)
		(DrawCel 995 1 1 4 3 temp51)
		(DrawCel 995 1 0 94 38 temp51)
		(DrawCel 995 1 0 135 38 temp51)
		(DrawCel
			995
			0
			4
			63
			(- 37 (+ (CelHigh 995 0 4) 3))
			temp51
		)
		(DrawCel
			995
			0
			3
			101
			(- 37 (+ (CelHigh 995 0 4) 3))
			temp51
		)
		(DrawCel
			995
			0
			2
			146
			(- 37 (+ (CelHigh 995 0 4) 3))
			temp51
		)
		(Graph grUPDATE_BOX 12 1 15 (+ 151 (CelWide 995 0 1)) 1)
		(SetPort 0)
	)
)

(instance detailSlider of Slider
	(properties
		view 995
		loop 0
		cel 1
		nsLeft 139
		nsTop 73
		signal $0080
		noun N_DETAILBUTTON
		helpVerb V_HELP
		sliderView 995
		bottomValue 1
		topValue 3
	)
	
	(method (doit param1)
		(if argc (gGame detailLevel: param1))
		(gGame detailLevel:)
	)
)

(instance volumeSlider of Slider
	(properties
		view 995
		loop 0
		cel 1
		nsLeft 179
		nsTop 73
		signal $0080
		noun N_VOLUME
		helpVerb V_HELP
		sliderView 995
		topValue 15
	)
)

(instance speedSlider of Slider
	(properties
		view 995
		loop 0
		cel 1
		nsLeft 219
		nsTop 73
		signal $0080
		noun N_SPEED
		helpVerb V_HELP
		sliderView 995
		bottomValue 15
	)
	
	(method (doit param1)
		(if argc (gEgo setSpeed: param1))
		(return gGEgoMoveSpeed)
	)
	
	(method (show)
		(if (not (gUser controls?))
			(= signal 132)
		else
			(= signal 128)
		)
		(super show: &rest)
	)
	
	(method (mask)
	)
	
	(method (move)
		(if (gUser controls?) (super move: &rest))
	)
)

(instance iconSave of ControlIcon
	(properties
		view 995
		loop 2
		cel 0
		nsLeft 80
		nsTop 42
		message 8
		signal $01c3
		noun N_SAVE
		helpVerb V_HELP
	)
)

(instance iconRestore of ControlIcon
	(properties
		view 995
		loop 3
		cel 0
		nsLeft 80
		nsTop 62
		message 8
		signal $01c3
		noun N_RESTORE
		helpVerb V_HELP
	)
)

(instance iconRestart of ControlIcon
	(properties
		view 995
		loop 4
		cel 0
		nsLeft 80
		nsTop 82
		message 8
		signal $01c3
		noun N_RESTART
		helpVerb V_HELP
	)
)

(instance iconQuit of ControlIcon
	(properties
		view 995
		loop 5
		cel 0
		nsLeft 80
		nsTop 102
		message 8
		signal $01c3
		noun N_QUITMENU
		helpVerb V_HELP
	)
)

(instance iconAbout of ControlIcon
	(properties
		view 995
		loop 6
		cel 0
		nsLeft 80
		nsTop 142
		message 8
		signal $01c3
		noun N_GAMEINFO
		helpVerb V_HELP
	)
)

(instance iconHelp of IconItem
	(properties
		view 995
		loop 7
		cel 0
		nsLeft 106
		nsTop 142
		cursor 989
		message V_HELP
		signal $0183
		noun N_HELPICON
		helpVerb V_HELP
	)
)

(instance iconOk of IconItem
	(properties
		view 995
		loop 8
		cel 0
		nsLeft 80
		nsTop 122
		cursor 989
		message 8
		signal $01c3
		noun N_RETURNTOGAME
		helpVerb V_HELP
	)
)

(instance iconMessageMode of IconItem
	(properties
		view 995
		loop 9
		cel 0
		nsLeft 137
		nsTop 143
		cursor 989
		message 8
		signal $0183
		noun N_MSGMODE
		helpVerb V_HELP
	)
	
	(method (doit)
		(switch gMessageType
			(1 (= gMessageType 2))
			(2 (= gMessageType 3))
			(3 (= gMessageType 1))
		)
		(self show:)
	)
	
	(method (show)
		(switch gMessageType
			(1
				(= global83 0)
				(DrawCel 995 10 0 188 141 -1)
			)
			(2
				(= global83 1)
				(DrawCel 995 10 1 188 141 -1)
			)
			(3
				(= global83 1)
				(DrawCel 995 10 2 188 141 -1)
			)
		)
		(Graph
			grUPDATE_BOX
			141
			188
			(+ 141 (CelHigh 995 10))
			(+ 188 (CelWide 995 10))
			1
		)
		(super show: &rest)
	)
)
