package com.lucaperri.niceeshotss

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lucaperri.niceeshotss.utils.UserProfileObject
import com.lucaperri.niceeshotss.utils.images.GallerySelectActivity
import com.lucaperri.niceeshotss.utils.images.Post
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException



class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var permissionsManager: PermissionsManager
    lateinit var tasks : CardView
    lateinit var pics : CardView
    lateinit var accountInfo: CardView
    lateinit var storage : StorageReference
    lateinit var viewAnnotationManager: ViewAnnotationManager



    private var permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {

        }
        override fun onPermissionResult(granted: Boolean) {
            if (granted) {
                with(mapView) {
                    location.locationPuck = createDefault2DPuck(withBearing = true)
                    location.enabled = true
                    location.puckBearing = PuckBearing.COURSE
                    viewport.transitionTo(
                        targetState = viewport.makeFollowPuckViewportState(),
                        transition = viewport.makeImmediateViewportTransition()
                    )
                }
            } else {

                // User denied the permission

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            with(mapView) {
                location.locationPuck = createDefault2DPuck(withBearing = true)
                location.enabled = true
                location.puckBearing = PuckBearing.HEADING
                viewport.transitionTo(
                    targetState = viewport.makeFollowPuckViewportState(),
                    transition = viewport.makeImmediateViewportTransition()
                )
            }
            permissionsManager = PermissionsManager(permissionsListener)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                1002)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1002)

        } else {
            permissionsManager = PermissionsManager(permissionsListener)
            permissionsManager.requestLocationPermissions(this)
        }

        tasks = findViewById(R.id.main_task_button)
        tasks.setOnClickListener {
            var intent : Intent = Intent(this, TasksViewActivity::class.java)
            startActivity(intent)
        }

        pics = findViewById(R.id.main_picture_button)
        pics.setOnClickListener {
            var dialog = Dialog(this)
            dialog.setContentView(R.layout.pic_dialogue)

            var gallery_btn = dialog.findViewById<Button>(R.id.dialog_roll)
            var snap = dialog.findViewById<Button>(R.id.dialog_now)
            gallery_btn.setOnClickListener {
                dialog.dismiss()
                //Open Gallery
                var intent : Intent = Intent(this@MainActivity, GallerySelectActivity::class.java)
                startActivity(intent)
            }
            snap.setOnClickListener {
                dialog.dismiss()
                //Open Camera Transaction
               dispatchTakePictureIntent()
            }
            dialog.show()
        }

        accountInfo = findViewById(R.id.main_account_button)
        accountInfo.setOnClickListener {
            var intent : Intent = Intent(this, ProfileViewActivity::class.java)
            startActivity(intent)
        }


        viewAnnotationManager = mapView.viewAnnotationManager
        var post_reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Posts")
       // fetchAllPosts(post_reference) FIXME You god damn monkey

    }



    fun fetchAllPosts(databaseReference: DatabaseReference) {
        Log.e("FetchMapData", "Fetching new MapData to display!")
        var user = FirebaseAuth.getInstance().currentUser!!
        var reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Registered Users")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("FetchMapData", "Fetching all users!")
                val profiles = mutableListOf<UserProfileObject>()
                for (postSnapshot in snapshot.children) {
                    val profile = postSnapshot.getValue(UserProfileObject::class.java)!!
                    profiles.add(profile)
                }

                for(p in profiles){
                    Log.e("FetchMapData", "Fetching all posts of "+p.userid)
                    databaseReference.child(p.userid).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot)
                        {


                            val posts = mutableListOf<Post>() // Assuming a Post class to hold post data

                            for (postSnapshot in dataSnapshot.children) {
                                val post = postSnapshot.getValue(Post::class.java)!!
                                posts.add(post)
                            }
                            Log.e("FetchMapData", "Lenght of posts: "+posts.size)
                            for(post :Post in posts){

                                var name = ""
                                Log.e("FetchMapData", "Fetching image: "+p.userid+"."+post.imgname)
                                val storageRef = FirebaseStorage.getInstance("gs://niceeshotss.appspot.com/").getReference("Images/"+p.userid+"."+post.imgname)
                                storageRef.downloadUrl.addOnCompleteListener { task -> run{
                                    if(task.isSuccessful){
                                        task.addOnSuccessListener { uri -> run{
                                            Log.e("FetchMapData", "Uri is: $uri")
                                            addViewAnnotationToPoint(point = Point.fromLngLat(post.longitude.toDouble(), post.latitude.toDouble()),name , user.uid+"."+post.imgname, uri, post.imgname)

                                        } }
                                    }
                                } }


                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle errors
                            Log.e("Firebase", "Error fetching posts: ${databaseError.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, 1100)
            }
        }
    }

    private fun addViewAnnotationToPoint(point: Point, owner: String, id: String, uri:Uri, imagename: String) {

        var viewAnnotation = viewAnnotationManager.addViewAnnotation(
            // Specify the layout resource id
            resId = R.layout.map_view,
            // Set any view annotation options
            options = viewAnnotationOptions {
                // View annotation is placed at the specific geo coordinate
                geometry(point)
            }
        )
        var title = viewAnnotation.findViewById<TextView>(R.id.mapview_title)
        title.text = "Image '"+imagename+"' by user: USER"
        var imageView = viewAnnotation.findViewById<ImageView>(R.id.mapview_imagecontainer)

        Picasso.with(this)
            .load(uri)
            .into(imageView)
        var hiddenID = viewAnnotation.findViewById<TextView>(R.id.mapview_hiddenatt)
        hiddenID.text = id

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("CameraActivity", "Pictuer has been taken, processing now...")
        if (requestCode == 1100 && resultCode == Activity.RESULT_OK) {
            Log.e("CameraActivity", "Picture is being written to memory..")
            val imageBitmap = data?.extras?.get("data") as Bitmap
            var dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.dialogue_okaypicture)
            var imageView = dialog.findViewById<ImageView>(R.id.checkpicture_image)
            imageView.setImageBitmap(imageBitmap)


            var submit = dialog.findViewById<Button>(R.id.check_submit)
            var retake = dialog.findViewById<Button>(R.id.check_retake)
            retake.setOnClickListener {
                dispatchTakePictureIntent()
                dialog.dismiss()
            }

            submit.setOnClickListener {
                Log.e("CameraActivity", "Saving Image to Storage")
                var filePath = saveImage(imageBitmap)
                if(filePath == null)
                    return@setOnClickListener
                var file = File(filePath)
                Log.e("CameraActivity", "Image has been saved!")
                if(file != null){
                    Log.e("CameraActivity", "Reading ExifData")
                    val exifInterface = ExifInterface(file.path)
                    val attrLATITUDE: String? = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                    val attrLATITUDE_REF: String? = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                    val attrLONGITUDE: String? = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                    val attrLONGITUDE_REF: String? =
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

                    var latitude = 0.0
                    var longitude = 0.0
                    if((attrLATITUDE !=null)
                        && (attrLATITUDE_REF !=null)
                        && (attrLONGITUDE != null)
                        && (attrLONGITUDE_REF !=null))
                    {

                        if(attrLATITUDE_REF.equals("N")){
                            latitude = convertToDegree(attrLATITUDE)?.toDouble()!!
                        }
                        else{
                            latitude = 0 - (convertToDegree(attrLATITUDE)?.toDouble()!!)
                        }

                        if(attrLONGITUDE_REF.equals("E")){
                            longitude = convertToDegree(attrLONGITUDE)?.toDouble()!!
                        }
                        else{
                            longitude = 0 - convertToDegree(attrLONGITUDE)?.toDouble()!!
                        }

                    }else{
                        Log.e("ExifReader", "There's no location tags!")
                    }

                    // Access other Exif tags as needed
                    val dateTimeOriginal = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    val make = exifInterface.getAttribute(ExifInterface.TAG_MAKE)
                    val model = exifInterface.getAttribute(ExifInterface.TAG_MODEL)


                    //FUCK FOV WE DO ZOOM
                    val fov = exifInterface.getAttributeDouble(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, 0.0)
                    val exp = exifInterface.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, 0.0)
                    val iso = exifInterface.getAttributeInt(ExifInterface.TAG_ISO_SPEED_RATINGS, 0)
                    val size = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 1920).toString()+"x"+exifInterface.getAttributeInt(
                        ExifInterface.TAG_IMAGE_LENGTH, 1080).toString()
                    val focal = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                    var user = FirebaseAuth.getInstance().currentUser!!
                    val files: StorageReference = storage.child(user.uid + "." + file.name.replace(".jpg", ""))
                    files.putFile(file.toUri()).addOnSuccessListener {
                        files.downloadUrl.addOnSuccessListener {uri ->
                            run {
                                var download: Uri = uri
                                var image_reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Posts")
                                image_reference.child(user.uid).child(file.name).setValue(Post(download.toString(), file.name.replace(".jpg", ""), dateTimeOriginal.toString(), iso.toString(), focal.toString(), exp.toString(), latitude.toString(), longitude.toString(), fov.toString(), model.toString()+"("+make+")", size.toString())).addOnCompleteListener {
                                        task -> run {
                                    if(task.isSuccessful){
                                        Toast.makeText(this, "Picture was uploaded!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }else{
                                        Toast.makeText(this, "Picture was uploaded!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                                }

                            }
                        }

                    }


                }
            }
            dialog.show()
        }
    }


    fun saveImageWithSAF(context: Context, imageBitmap: Bitmap) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_TITLE, "image_${System.currentTimeMillis()}.jpg")

        }

        (context as Activity).startActivityForResult(intent, 1101)
    }
    private fun saveImage(imageBitmap: Bitmap): String? {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        Log.e("CameraActivity", "Creating directory")
        val dir = File(root, "NiceeShotss")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        Log.e("CameraActivity", "Creating Fileobject")
        val file = File(dir, "image_${System.currentTimeMillis()}.jpg")
        try {
            val out = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            return file.path
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    private fun convertToDegree(stringDMS: String): Float? {
        var result: Float? = null
        val DMS = stringDMS.split(",".toRegex(), limit = 3).toTypedArray()
        val stringD = DMS[0].split("/".toRegex(), limit = 2).toTypedArray()
        val D0: Double = stringD[0].toDouble()
        val D1: Double = stringD[1].toDouble()
        val FloatD = D0 / D1
        val stringM = DMS[1].split("/".toRegex(), limit = 2).toTypedArray()
        val M0: Double = stringM[0].toDouble()
        val M1: Double = stringM[1].toDouble()
        val FloatM = M0 / M1
        val stringS = DMS[2].split("/".toRegex(), limit = 2).toTypedArray()
        val S0: Double = stringS[0].toDouble()
        val S1: Double = stringS[1].toDouble()
        val FloatS = S0 / S1
        result = (FloatD + FloatM / 60 + FloatS / 3600).toFloat()
        return result
    };
}