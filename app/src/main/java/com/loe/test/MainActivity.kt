package com.loe.test

import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.loe.json_ext.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 字符串转Json
        val json = Json("{'名称':'顺哥哥','age':26,'sex':true,'资金':123.56}")

        // --加入复杂列表结构
        json.put("childList", JsonArray()
            .put(Json("name","小不点").put("age",66))
            .put(Json().put("name","汤姆")))

        // Json转Bean
        val people = json.toBean<People>()

        // Bean转Json
        val json2 = people.toJson()

        textView.text = json2.toString(4)
    }

    @Keep
    class People
    {
        @JsonName("名称")
        var name:String = ""

        var age = 0

        var sex = true

        @JsonName("资金")
        var money: Double? = null

        var childList: List<Child>? = null
    }

    @Keep
    class Child
    {
        var name = ""

        var age: Int? = null
    }
}