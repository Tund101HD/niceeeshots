package com.lucaperri.niceeshotss.utils.images

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lucaperri.niceeshotss.ProfileViewActivity
import com.lucaperri.niceeshotss.R
import com.lucaperri.niceeshotss.utils.FileUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException


class GallerySelectActivity : Activity(){

    lateinit var auth : FirebaseAuth
    lateinit var imgname: TextView
    lateinit var date: TextView
    lateinit var iso: TextView
    lateinit var focal: TextView
    lateinit var exposure: TextView
    lateinit var latitude: TextView
    lateinit var longitude: TextView
    lateinit var fov: TextView
    lateinit var model: TextView
    lateinit var size: TextView

    lateinit var refine: Button
    lateinit var submit: Button
    lateinit var choose: Button
    lateinit var preview: ImageView
    lateinit var cardView: CardView
    lateinit var subtext: TextView

    lateinit var storage : StorageReference
    lateinit var user : FirebaseUser
    final var PICK_IMAGE_REQUEST = 1001
    final var REQUEST_READ_EXTERNAL_STORAGE = 1002
    final var REQUEST_READ_STORAGE = 1003

    lateinit var uri: Uri
    lateinit var imageName : String




    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galleryselect)
        imgname = findViewById(R.id.gallery_display_imgname)
        date = findViewById(R.id.gallery_display_date)
        iso = findViewById(R.id.gallery_display_ISO)
        focal = findViewById(R.id.gallery_display_focal)
        exposure = findViewById(R.id.gallery_display_exp)
        latitude = findViewById(R.id.gallery_display_lat)
        longitude = findViewById(R.id.gallery_display_long)
        fov = findViewById(R.id.gallery_display_fov)
        model = findViewById(R.id.gallery_display_type)
        size = findViewById(R.id.gallery_display_size)
        refine = findViewById(R.id.gallery_refinebutton)
        submit = findViewById(R.id.gallery_submit)
        choose= findViewById(R.id.gallery_choose)
        preview = findViewById(R.id.gallery_preview)
        cardView = findViewById(R.id.gallery_cardview)
        subtext = findViewById(R.id.gallery_submittext)

        choose.setOnClickListener { openFileChooser() }
        submit.setOnClickListener { uploadImage() }
        refine.setOnClickListener {
            var dialog = Dialog(this)
            dialog.setContentView(R.layout.refine_dialogue)
            var submitbtn = dialog.findViewById<Button>(R.id.dialog_submitrefine)
            var iso = dialog.findViewById<TextView>(R.id.dialog_ISO)
            var lat = dialog.findViewById<TextView>(R.id.dialog_lat)
            var long = dialog.findViewById<TextView>(R.id.dialog_long)
            var model = dialog.findViewById<TextView>(R.id.dialog_cam)
            var exposure = dialog.findViewById<TextView>(R.id.dialog_exp)
            iso.text = this.iso.text
            lat.text = this.latitude.text
            long.text = this.longitude.text
            model.text = this.model.text
            exposure.text = this.exposure.text
            submitbtn.setOnClickListener {
                if(!StringUtils.isEmpty(iso.text)) this.iso.text = iso.text
                if(!StringUtils.isEmpty(lat.text)) this.latitude.text = lat.text
                if(!StringUtils.isEmpty(long.text)) this.longitude.text = long.text
                if(!StringUtils.isEmpty(model.text)) this.model.text = model.text
                if(!StringUtils.isEmpty(exposure.text)) this.exposure.text = exposure.text
                dialog.dismiss()
            }
            dialog.show()

        }

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            REQUEST_READ_EXTERNAL_STORAGE)

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
            REQUEST_READ_EXTERNAL_STORAGE)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission_group.STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE)
        }


        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        storage = FirebaseStorage.getInstance().getReference("Images")


    }

    fun openFileChooser(){
        var intent: Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null){
            Log.e("ActivityResultChangeImage", "Received image data, parsing data now")
            var image = data.data!!
            uri = image!!
            Log.e("ActivityResultChangeImage", "Got image URI")
            preview.visibility = ImageView.VISIBLE
            preview.setImageURI(null);
            preview.invalidate()
            preview.setImageURI(image)
            Log.e("ActivityResultChangeImage", "Set image from uri, parsing exif")
            val filePath = FileUtils.getPathFromUri(this, image)
            var metadata: Metadata? = ImageMetadataReader.readMetadata(File(filePath))
            try {
                Log.e("ActivityResultChangeImage", "Setting values")
                cardView.visibility = CardView.VISIBLE
                preview.visibility = ImageView.VISIBLE
                submit.visibility = Button.VISIBLE
                subtext.visibility = TextView.VISIBLE
                val exifInterface = ExifInterface(filePath)
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
                val size = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 1920).toString()+"x"+exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 1080).toString()
                val focal = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                Log.e("ImageUtils", "Image Name ist: "+ FileUtils.getFileName(uri.toString()).toString())
                imgname.text = FileUtils.getFileName(uri.toString()).toString().removePrefix("image%")
                Log.e("ImageUtils", "Date is: "+dateTimeOriginal)
                this.date.text = dateTimeOriginal.toString()
                Log.e("ImageUtils", "ISO is: "+iso)
                Log.e("ImageUtils", "Latitude is: "+latitude)
                this.iso.text = iso.toString()
                this.focal.text = focal.toString()
                this.exposure.text = exp.toString()
                this.latitude.text = latitude.toString()
                this.longitude.text = longitude.toString()
                this.fov.text = fov.toString()
                this.size.text = size
                this.model.text = model.toString()+" ("+make+")"
                imageName = imgname.text.toString()
            } catch (e: IOException) {
                Log.e("ExifData", "Error reading Exif data: ${e.message}")
            }
        }else{
            Toast.makeText(this, "Something went wrong trying to fetch image", Toast.LENGTH_SHORT)
        }
    }

    fun uploadImage(){
        if(uri != null) run {
            val file: StorageReference = storage.child(user.uid + "." + imageName)
            file.putFile(uri).addOnSuccessListener {
                file.downloadUrl.addOnSuccessListener {uri ->
                    run {
                        var download: Uri = uri
                        var reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Registered Users")
                        var image_reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Posts")
                        image_reference.child(user.uid).child(imageName).setValue(Post(download.toString(), imageName, date.text.toString(), iso.text.toString(), focal.text.toString(), exposure.text.toString(), latitude.text.toString(), longitude.text.toString(), fov.text.toString(), model.text.toString(), size.text.toString())).addOnCompleteListener {
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