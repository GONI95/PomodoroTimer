package sang.gondroid.pomodorotimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import sang.gondroid.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Gon : SeekBar를 조작할 때 마다 CountDownTimer 새로 생성하기 때문에 null인 경우에만 생성하기 위해서 선언
    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
        controlMinutesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateRemainTime(progress * 60 * 1000L)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                currentCountDownTimer?.cancel()
                currentCountDownTimer = null
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar == null) return

                currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L).start()
                currentCountDownTimer?.start()
            }
        })
    }

    // Gon : 종료까지 남은 밀리초, onTick() 메서드 호출 간격
    private fun createCountDownTimer(millis: Long): CountDownTimer =
        object: CountDownTimer(millis, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                updateRemainTime(0)
                updateSeekBar(0)
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
}