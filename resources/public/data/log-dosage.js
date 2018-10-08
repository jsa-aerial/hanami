{
	"height": 600,
	"width": 600,
	"data": {
		"values": [
			{
				"dose": 0.5,
				"response": 32659
			},
			{
				"dose": 0.5,
				"response": 40659
			},
			{
				"dose": 0.5,
				"response": 29000
			},
			{
				"dose": 1,
				"response": 31781
			},
			{
				"dose": 1,
				"response": 30781
			},
			{
				"dose": 1,
				"response": 35781
			},
			{
				"dose": 2,
				"response": 30054
			},
			{
				"dose": 4,
				"response": 29398
			},
			{
				"dose": 5,
				"response": 27779
			},
			{
				"dose": 10,
				"response": 27915
			},
			{
				"dose": 15,
				"response": 27410
			},
			{
				"dose": 20,
				"response": 25819
			},
			{
				"dose": 50,
				"response": 23999
			},
			{
				"dose": 50,
				"response": 25999
			},
			{
				"dose": 50,
				"response": 20999
			}
		]
	},
	"layer": [
		{
			"selection": {
				"grid": {
					"type": "interval",
					"bind": "scales",
					"on": "[mousedown, window:mouseup] > window:mousemove!",
					"encodings": ["x", "y"],
					"translate": "[mousedown, window:mouseup] > window:mousemove!",
					"zoom": "wheel!",
					"mark": {
						"fill": "#333",
						"fillOpacity": 0.125,
						"stroke": "white"
					},
					"resolve": "global"
				}
			},
			"mark": {
				"type": "point",
				"filled": true,
				"color": "green"
			},
			"encoding": {
				"x": {
					"field": "dose",
					"type": "quantitative",
					"scale": {
						"type": "log"
					}
				},
				"y": {
					"field": "response",
					"type": "quantitative",
					"aggregate": "mean"
				}
			}
		},
		{
			"mark": {
				"type": "errorbar",
				"ticks": true
			},
			"encoding": {
				"x": {
					"field": "dose",
					"type": "quantitative",
					"scale": {
						"zero": false
					}
				},
				"y": {
					"field": "response",
					"type": "quantitative"
				},
				"color": {
					"value": "#4682b4"
				}
			}
		}
	]
}
