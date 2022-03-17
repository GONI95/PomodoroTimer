package sang.gondroid.pomodorotimer

import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.SeekBar
import sang.gondroid.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Gon : SeekBar를 조작할 때 마다 CountDownTimer 새로 생성하기 때문에 null인 경우에만 생성하기 위해서 선언
    private var currentCountDownTimer: CountDownTimer? = null

    private val soundPoll = SoundPool.Builder().build()
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    override fun onResume() {
        super.onResume()

        soundPoll.autoResume()
    }

    // Gon : 사운드는 특정 앱에서만 리소스를 가지는 것이 아님, 디바이스에서 가지는 사운드에 재생을 요청하기 때문에 앱에서 멈추도록 처리
    override fun onPause() {
        super.onPause()

        soundPoll.autoPause()
    }

    // Gon : 앱을 사용하지 않는 경우 리소스를 해제(soundPoll에 로드외었던 파일들이 해제됨)
    override fun onDestroy() {
        super.onDestroy()

        soundPoll.release()
    }

    private fun init() = with(binding) {

        /*
        Gon : onStopTrackingTouch()로 CountDownTimer 객체가 생성되고 updateSeekBar()를 통해
              SeekBar의 Progress를 계속해서 업데이트 하는데 이 과정에서 updateSeekBar()의 작업이
              onProgressChanged()를 호출하여 00초를 출력하게 되면서 값이 한번씩 57~58초로 바로 뜀

              해결 방법 : fromUser를 통해 사용자의 이벤트인 경우에만 동작하도록 변경
         */
        controlMinutesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    updateRemainTime(progress * 60 * 1000L)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopCountDown()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar == null) return

                if (seekBar.progress == 0) stopCountDown()
                else startCountDown(seekBar)
            }
        })

        tickingSoundId = soundPoll.load(this@MainActivity, R.raw.timer_tick, 1)
        bellSoundId = soundPoll.load(this@MainActivity, R.raw.timer_bell, 1)
    }

    // Gon : 종료까지 남은 밀리초, onTick() 메서드 호출 간격
    private fun createCountDownTimer(millis: Long): CountDownTimer =
        object: CountDownTimer(millis, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }

    private fun updateRemainTime(millis: Long) = with(binding) {
        val remainSeconds = millis / 1000

        // Gon : 남은 전체 시간을 초로 받기 때문에 분,초를 표현하기 위해서 60(1분)으로 값 처리
        remainMinutesTextView.text = resources.getString(R.string.minutesFormat, remainSeconds / 60)
        remainSecondsTextView.text = resources.getString(R.string.secondsFormat, remainSeconds % 60)
    }

    private fun updateSeekBar(millis: Long) = with(binding) {
        // Gon : 남은 전체 밀리초로 받기 때문에 분 단위의 SeekBar를 표현하기 위해서 60000(1분)으로 값 처리
        controlMinutesSeekBar.progress = (millis / 60000).toInt()
    }

    private fun startCountDown(seekBar: SeekBar) {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L).start()
        currentCountDownTimer?.start()

        //Gon : loop forever -> -1
        tickingSoundId?.let {
            soundPoll.play(it, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPoll.autoPause()
    }

    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)

        bellSoundId?.let {
            soundPoll.play(it, 1F, 1F, 0, 0, 1F)
        }
    }
}