package com.iMadeASimpleApp.Mqtt.Hikari

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttClient.generateClientId
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttSecurityException


fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var light: Sensor? = null
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var mqqtTopic: String
    private lateinit var onOffButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        onOffButton = findViewById(R.id.onOffButton) as Button
        onOffButton.setOnClickListener(){
            if (isMqqtConnected()){
                disconnectMqtt()
            }
            else {
                connectMqtt()
            }
        }

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    private fun disconnectMqtt() {
        mqttAndroidClient.disconnect()
        onOffButton.text = getString(R.string.onOffButton)
    }


    private fun connectMqtt() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this) as SharedPreferences
        val nothing = ""
        val serverUri = sharedPref.getString("serveruri", nothing) as String
        val clientId = generateClientId()
        val username = sharedPref.getString("username", nothing) as String
        val password = sharedPref.getString("password", nothing) as String
        mqqtTopic = sharedPref.getString("subscriptionTopic", nothing) as String

        if (serverUri != nothing && username != nothing && password != nothing && mqqtTopic != nothing) {
            try {
                mqttAndroidClient = MqttAndroidClient(this.applicationContext, serverUri, clientId)

                val mqttConnectOptions = MqttConnectOptions()
                mqttConnectOptions.setAutomaticReconnect(true)
                mqttConnectOptions.setCleanSession(false)
                mqttConnectOptions.setUserName(username)
                mqttConnectOptions.setPassword(password.toCharArray())

                mqttAndroidClient.connect(mqttConnectOptions)

                onOffButton.text = getString(R.string.stopMqttBroadcast)

            }
            catch (e: MqttException){
                toast("Failed to establish connection")
            }
            catch (e: MqttSecurityException){
                toast("Failed to establish connection due to security reason")
            }
        } else {
            toast("Valid configuration not found, go to settings")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    private fun isMqqtConnected(): Boolean {
        if (::mqttAndroidClient.isInitialized) {
            if (mqttAndroidClient.isConnected()) {
                return true
            }
        }
        return false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val amountOfLight= event.values[0]
        val textView: TextView = findViewById(R.id.currentBrightnessDisplay) as TextView
        textView.text = amountOfLight.toString()
        if (isMqqtConnected()) {
            val message = MqttMessage(amountOfLight.toString().toByteArray())
            this.mqttAndroidClient.publish(mqqtTopic, message)
        }
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager.unregisterListener(this)
    }


}

