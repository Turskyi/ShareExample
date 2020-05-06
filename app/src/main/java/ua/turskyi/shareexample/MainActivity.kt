package ua.turskyi.shareexample

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.widget.ShareDialog
import kotlinx.android.synthetic.main.activity_main.*
import splitties.toast.toast
import ua.turskyi.shareexample.Constant.GOOGLE_PLAY_ADDRESS
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val animationDrawable: AnimationDrawable =
            layout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        btnShare.setOnClickListener {
            if (isFacebookInstalled(this)) {
                shareViaFacebook()
            } else {
                shareImageViaChooser()
            }
        }
    }

    private fun storeFileAs(bitmap: Bitmap, fileName: String): File {
        val dirPath =
            externalCacheDir?.absolutePath + "/Screenshots"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun shareImageViaChooser() {
        val fileName =
            "example${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())}.jpg"
        val bitmap = getScreenShot(layout)
        val file = bitmap?.let { storeFileAs(it, fileName) }
        val uri = file?.let {
            FileProvider.getUriForFile(
                this,
                applicationContext.packageName.toString() + ".provider",
                it
            )
        }

        val intentImage = Intent()
        intentImage.action = Intent.ACTION_SEND
        intentImage.type = "image/*"
        intentImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentImage.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
        intentImage.putExtra(Intent.EXTRA_TEXT, "#travelling_the_world \n $GOOGLE_PLAY_ADDRESS")
        intentImage.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(
                Intent.createChooser(
                    intentImage,
                    "Share How Many Countries You Have Visited"
                )
            )
        } catch (e: ActivityNotFoundException) {
            toast("No app available to share pie chart")
        }
    }

    private fun View.mapViewToBitmap(): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    private fun getScreenShot(view: View): Bitmap? {
        return view.mapViewToBitmap()?.let { Bitmap.createBitmap(it) }
    }

    private fun isFacebookInstalled(context: Context): Boolean {
        val packageManager: PackageManager = context.packageManager
        try {
            packageManager.getPackageInfo("com.facebook.katana", PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    private fun shareViaFacebook() {
        val webAddress =
            ShareHashtag.Builder().setHashtag("#example_hashtag \n $GOOGLE_PLAY_ADDRESS")
                .build()
        val bitmap = getScreenShot(layout)
        val sharePhoto = SharePhoto.Builder().setBitmap(bitmap).setCaption(
            "example${SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            ).format(Date())}"
        )
            .build()
        val mediaContent = ShareMediaContent.Builder()
            .addMedium(sharePhoto)
            .setShareHashtag(webAddress)
            .build()
        ShareDialog.show(this, mediaContent)
    }
}
