package com.example.socketio

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.socket.client.IO
import io.socket.client.Socket

class SocketService : Service() {
    lateinit var  socket: Socket
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Log.d("SocketService", "Service Created")

        // Get the socket instance from the singleton
        socket = IO.socket("http://192.168.1.103:3002/")

        // Connect to the socket
        socket.connect()

        // Add listeners
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "Socket connected")
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketService", "Socket disconnected")
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketService", "Error Socket")
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Initialize LocationRequest using the new Builder pattern
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1000) // Set fastest interval (e.g., 5 seconds)
            .build()

        // Create a LocationCallback to handle location changes
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.let {
                    for (location in it.locations) {
                        // Handle the location update
                        Log.d("Location", "Lat: ${location.latitude}, Lon: ${location.longitude}")
                        socket.emit("message", "Lat: ${location.latitude}, Lon: ${location.longitude}")
                    }
                }
            }
        }

        // get location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        startForegroundService()
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val value = intent?.getStringExtra("location")
//        Log.d("location", value!!)
//        return START_STICKY
//    }

    private fun startForegroundService() {
        val channelId = "socket_service_channel"
        val channelName = "Socket Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Socket Service Running")
                .setContentText("Socket.IO connection is active")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
                .build()
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SocketService", "Service Destroyed")
        // Disconnect the socket when the service is destroyed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not binding this service
    }
}
