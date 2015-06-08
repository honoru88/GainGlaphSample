(function () {
	'use strict';

	// 브라우저가 실제로 비디오 요소를 지원합니까?
	var supportsVideo = !!document.createElement('video').canPlayType;

	if (supportsVideo) {
		// Obtain handles to main elements
		var videoContainer = document.getElementById('videoContainer');
		var video = document.getElementById('video');
		var videoControls = document.getElementById('video-controls');

		// 기본 컨트롤을 숨기기
		video.controls = false;

		// 사용자 정의 비디오 컨트롤을 표시
		videoControls.setAttribute('data-state', 'visible');

		// 버튼 및 기타 요소에 대한 핸들을 얻습니다
		var playpause = document.getElementById('playpause');
		var stop = document.getElementById('stop');
		var mute = document.getElementById('mute');
		var volinc = document.getElementById('volinc');
		var voldec = document.getElementById('voldec');
		var progress = document.getElementById('progress');
		var progressBar = document.getElementById('progress-bar');
		var fullscreen = document.getElementById('fs');
		var subtitles = document.getElementById('subtitles');

		// If the browser doesn't support the progress element, set its state for some different styling 브라우저가 진행 요소를 지원하지 않는다면, 어떤 다른 스타일링 상태를 설정할
		var supportsProgress = (document.createElement('progress').max !== undefined);
		if (!supportsProgress) progress.setAttribute('data-state', 'fake');

		// 브라우저가 전체 화면 API를 지원하는지 확인
		var fullScreenEnabled = !!(document.fullscreenEnabled || document.mozFullScreenEnabled || document.msFullscreenEnabled || document.webkitSupportsFullscreen || document.webkitFullscreenEnabled || document.createElement('video').webkitRequestFullScreen);
		// 브라우저 Fulscreen API를 지원하지 않는 경우 전체 화면 버튼을 숨기기
		if (!fullScreenEnabled) {
			fullscreen.style.display = 'none';
		}

		// Check the volume
		var checkVolume = function(dir) {
			if (dir) {
				var currentVolume = Math.floor(video.volume * 10) / 10;
				if (dir === '+') {
					if (currentVolume < 1) video.volume += 0.1;
				}
				else if (dir === '-') {
					if (currentVolume > 0) video.volume -= 0.1;
				}
				// If the volume has been turned off, also set it as muted
				// Note: 단지 'volumechange'이벤트가 발생할 때로 설정된 사용자 지정 컨트롤로이 작업을 수행 할 수 있습니다, 그것은 볼륨이나 음소거 변화에 의한 것이면 알 수있는 방법이 없습니다
				if (currentVolume <= 0) video.muted = true;
				else video.muted = false;
			}
			changeButtonState('mute');
		}

		// Change the volume
		var alterVolume = function(dir) {
			checkVolume(dir);
		}

		// Set the video container's fullscreen state
		var setFullscreenData = function(state) {
			videoContainer.setAttribute('data-fullscreen', !!state);
			// Set the fullscreen button's 'data-state' which allows the correct button image to be set via CSS
			fullscreen.setAttribute('data-state', !!state ? 'cancel-fullscreen' : 'go-fullscreen');
		}

		// Checks if the document is currently in fullscreen mode
		var isFullScreen = function() {
			return !!(document.fullScreen || document.webkitIsFullScreen || document.mozFullScreen || document.msFullscreenElement || document.fullscreenElement);
		}

		// Fullscreen
		var handleFullscreen = function() {
			// If fullscreen mode is active...
			if (isFullScreen()) {
					// ...exit fullscreen mode
					// (Note: this can only be called on document)
					if (document.exitFullscreen) document.exitFullscreen();
					else if (document.mozCancelFullScreen) document.mozCancelFullScreen();
					else if (document.webkitCancelFullScreen) document.webkitCancelFullScreen();
					else if (document.msExitFullscreen) document.msExitFullscreen();
					setFullscreenData(false);
				}
				else {
					// ...otherwise enter fullscreen mode
					// (Note: 그것은 또한 예를 들어 요소의 아이들, 보장하므로 문서를 호출 할 수 있지만, 여기에 특정 요소가 사용됩니다사용자 지정 컨트롤은 전체 화면도 이동합니다)
					if (videoContainer.requestFullscreen) videoContainer.requestFullscreen();
					else if (videoContainer.mozRequestFullScreen) videoContainer.mozRequestFullScreen();
					else if (videoContainer.webkitRequestFullScreen) {
					    //사파리 5.1은 비디오 요소에 적절한 전체 화면을 수 있습니다. 이것은 또한 다시 나타나는 기본 컨트롤을 숨 깁니다 (styles.css가 설정) 다음 CSS와 같은 다른 웹킷 브라우저에서 잘 작동하고 사용자 지정 컨트롤이 표시되는지 확인합니다
						// Safari 5.1 only allows proper fullscreen on the video element. This also works fine on other WebKit browsers as the following CSS (set in styles.css) hides the default controls that appear again, and
						// ensures that our custom controls are visible:
						// figure[data-fullscreen=true] video::-webkit-media-controls { display:none !important; }
						// figure[data-fullscreen=true] .controls { z-index:2147483647; }
						video.webkitRequestFullScreen();
					}
					else if (videoContainer.msRequestFullscreen) videoContainer.msRequestFullscreen();
					setFullscreenData(true);
				}
			}

		// addEventListener를이 지원되는 경우에만 이벤트를 추가 (지원하지 않는 IE8 덜하지만 어쨌든 플래시를 사용합니다)
		if (document.addEventListener) {
			// Wait for the video's meta data to be loaded, then set the progress bar's max value to the duration of the video
			video.addEventListener('loadedmetadata', function() {
				progress.setAttribute('max', video.duration);
			});

			// 특정 버튼의의 버튼 상태를 변경 그래서 올바른 시각은 CSS로 표시 할 수 있습니다
			var changeButtonState = function(type) {
				// Play/Pause button
				if (type == 'playpause') {
					if (video.paused || video.ended) {
						playpause.setAttribute('data-state', 'play');
					}
					else {
						playpause.setAttribute('data-state', 'pause');
					}
				}
				// Mute button
				else if (type == 'mute') {
					mute.setAttribute('data-state', video.muted ? 'unmute' : 'mute');
				}
			}

			// Add event listeners for video specific events
			video.addEventListener('play', function() {
				changeButtonState('playpause');
			}, false);
			video.addEventListener('pause', function() {
				changeButtonState('playpause');
			}, false);
			video.addEventListener('volumechange', function() {
				checkVolume();
			}, false);

			// Add events for all buttons
			playpause.addEventListener('click', function(e) {
				if (video.paused || video.ended) video.play();
				else video.pause();
			});

			// Turn off all subtitles
			for (var i = 0; i < video.textTracks.length; i++) {
				video.textTracks[i].mode = 'hidden';
			}

			// 작성해 자막 언어 메뉴의 메뉴 항목을 리턴
			var subtitleMenuButtons = [];
			var createMenuItem = function(id, lang, label) {
				var listItem = document.createElement('li');
				var button = listItem.appendChild(document.createElement('button'));
				button.setAttribute('id', id);
				button.className = 'subtitles-button';
				if (lang.length > 0) button.setAttribute('lang', lang);
				button.value = label;
				button.setAttribute('data-state', 'inactive');
				button.appendChild(document.createTextNode(label));
				button.addEventListener('click', function(e) {
					// Set all buttons to inactive
					subtitleMenuButtons.map(function(v, i, a) {
						subtitleMenuButtons[i].setAttribute('data-state', 'inactive');
					});
					// Find the language to activate
					var lang = this.getAttribute('lang');
					for (var i = 0; i < video.textTracks.length; i++) {
						// For the 'subtitles-off' button, the first condition will never match so all will subtitles be turned off
						if (video.textTracks[i].language == lang) {
							video.textTracks[i].mode = 'showing';
							this.setAttribute('data-state', 'active');
						}
						else {
							video.textTracks[i].mode = 'hidden';
						}
					}
					subtitlesMenu.style.display = 'none';
				});
				subtitleMenuButtons.push(button);
				return listItem;
			}
			// 각 하나를 통해 이동 작은 클릭 가능한 목록을 작성하고, 각 항목에 클릭 할 때에는 "showing"하고 다른 사람들이 "hidden"로 그것의 모드를 설정
			var subtitlesMenu;
			if (video.textTracks) {
				var df = document.createDocumentFragment();
				var subtitlesMenu = df.appendChild(document.createElement('ul'));
				subtitlesMenu.className = 'subtitles-menu';
				subtitlesMenu.appendChild(createMenuItem('subtitles-off', '', 'Off'));
				for (var i = 0; i < video.textTracks.length; i++) {
					subtitlesMenu.appendChild(createMenuItem('subtitles-' + video.textTracks[i].language, video.textTracks[i].language, video.textTracks[i].label));
				}
				videoContainer.appendChild(subtitlesMenu);
			}
			subtitles.addEventListener('click', function(e) {
				if (subtitlesMenu) {
					subtitlesMenu.style.display = (subtitlesMenu.style.display == 'block' ? 'none' : 'block');
				}
			});

			// 미디어 API에는 'STOP ()'기능이 없기 때문에 동영상을 일시 정지하고 시간과 진행 표시 줄을 다시 설정
			stop.addEventListener('click', function(e) {
				video.pause();
				video.currentTime = 0;
				progress.value = 0;
				// 올바른 버튼 이미지가 CSS를 통해 설정할 수 있습니다 재생 / 일시 정지 버튼의 '데이터 상태를'업데이트
				changeButtonState('playpause');
			});
			mute.addEventListener('click', function(e) {
				video.muted = !video.muted;
				changeButtonState('mute');
			});
			volinc.addEventListener('click', function(e) {
				alterVolume('+');
			});
			voldec.addEventListener('click', function(e) {
				alterVolume('-');
			});
			fs.addEventListener('click', function(e) {
				handleFullscreen();
			});

			// As the video is playing, update the progress bar
			video.addEventListener('timeupdate', function() {
				// 모바일 브라우저의 경우, 진행 요소의 최대 속성이 설정되어 있는지 확인
				if (!progress.getAttribute('max')) progress.setAttribute('max', video.duration);
				var p_currentTime=parseInt(video.currentTime);
				var p_duration=parseInt(video.duration);
				var p_value=((p_currentTime / p_duration) * 100) ;
				progress.value = p_value;//이거함보자
				progressBar.style.width = Math.floor((video.currentTime / video.duration) * 100) + '%';

			});

			// React to the user clicking within the progress bar
			progress.addEventListener('click', function(e) {
				// Also need to take the parents into account here as .controls and figure now have position:relative
				var pos = (e.pageX  - (this.offsetLeft + this.offsetParent.offsetLeft + this.offsetParent.offsetParent.offsetLeft)) / this.offsetWidth;
				video.currentTime = pos * video.duration;
			});

			// (다른 컨트롤, 비디오 자체에 대한 예를 들어, 마우스 오른쪽 버튼을 클릭에서) 전체 화면 변경 이벤트를 수신
			document.addEventListener('fullscreenchange', function(e) {
				setFullscreenData(!!(document.fullScreen || document.fullscreenElement));
			});
			document.addEventListener('webkitfullscreenchange', function() {
				setFullscreenData(!!document.webkitIsFullScreen);
			});
			document.addEventListener('mozfullscreenchange', function() {
				setFullscreenData(!!document.mozFullScreen);
			});
			document.addEventListener('msfullscreenchange', function() {
				setFullscreenData(!!document.msFullscreenElement);
			});
		}
	 }

 })();