package com.justcode.hxl.action


import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import android.util.Log

import java.io.DataOutputStream
import android.content.Intent
import android.util.AndroidException
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.Context.POWER_SERVICE
import android.os.PowerManager




class MainActivity : AppCompatActivity() {
    var workX = 350
    var workY = 1200
    var kaoqingX = 90
    var kaoqingY = 780
    var dakaX = 340
    var dakaY = 480
    var date_string = "2018-09-30 08:30"
    var disposable: Disposable? = null
    var dataLong: Long? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn_sure.setOnClickListener {

            workX = et_gongzuo_x.text.toString().toInt()
            workY = et_gongzuo_y.text.toString().toInt()
            kaoqingX = et_kaoqing_x.text.toString().toInt()
            kaoqingY = et_kaoqing_y.text.toString().toInt()
            dakaX = et_daka_x.text.toString().toInt()
            dakaY = et_daka_y.text.toString().toInt()

            date_string = et_year.text.toString() + "-" + et_month.text.toString() + "-" + et_day.text.toString() + " " + et_hour.text.toString() + ":" + et_min.text.toString()


            dataLong = string2Long(date_string)

            startLunxun()
        }


    }

    fun startLunxun() {
        if (disposable == null) {
            disposable = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        dataLong?.let {
                            if (dataLong!! - System.currentTimeMillis() <= 0) {
                                wakeUp()
                            }
                        }

                    }
        }

    }

     fun wakeUp() {
         // 获取电源管理器对象
         val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
         val screenOn = pm.isScreenOn
         if (!screenOn) {
             // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
             val wl = pm.newWakeLock(
                     PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright")
             wl.acquire(10000) // 点亮屏幕
             wl.release() // 释放
         }
         startTask2Dingding()
    }

    fun string2Long(date_string: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = formatter.parse(date_string)
        val time = date.time
        return time
    }

    fun startTask2Dingding() {

        Handler().postDelayed({
            start2Ding()
        }, 0)
        Handler().postDelayed({
            exe("input tap $workX $workY")
            Log.d("dingdingrizhi", "工作")
        }, 15000)
        Handler().postDelayed({
            exe("input tap $kaoqingX $kaoqingY")
            Log.d("dingdingrizhi", "考勤打卡")
        }, 18000)
        Handler().postDelayed({
            exe("input tap $dakaX $dakaY")
            Log.d("dingdingrizhi", "打卡")
        }, 25000)
    }


    fun start2Ding() {
        var intent = Intent()
        // 这里的packname就是从上面得到的目标apk的包名
        intent = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet") //钉钉包名
        startActivity(intent)
        disposable?.dispose()
        disposable = null
    }

    fun exe(cmd: String) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            val process = Runtime.getRuntime().exec("su")
            // 获取输出流
            val outputStream = process.outputStream
            val dataOutputStream = DataOutputStream(
                    outputStream)
            dataOutputStream.writeBytes(cmd)
            dataOutputStream.flush()
            dataOutputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.d("dingdingrizhi", "异常：" + e.message)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        disposable = null
    }
}
