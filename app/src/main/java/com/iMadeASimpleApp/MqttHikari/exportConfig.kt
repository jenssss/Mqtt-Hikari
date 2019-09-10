
//import android.content.Context
//import androidx.room.*
//
//@Entity
//data class User(
//    @PrimaryKey val uid: Int,
//    @ColumnInfo(name = "first_name") val firstName: String?,
//    @ColumnInfo(name = "last_name") val lastName: String?
//)
//
//@Dao
//interface UserDao {
//    @Query("SELECT * FROM user")
//    fun getAll(): List<User>
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User
//
//    @Insert
//    fun insertAll(vararg users: User)
//
//    @Delete
//    fun delete(user: User)
//}
//
//open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
//    private var creator: ((A) -> T)? = creator
//    @Volatile private var instance: T? = null
//
//    fun getInstance(arg: A): T {
//        val i = instance
//        if (i != null) {
//            return i
//        }
//
//        return synchronized(this) {
//            val i2 = instance
//            if (i2 != null) {
//                i2
//            } else {
//                val created = creator!!(arg)
//                instance = created
//                creator = null
//                created
//            }
//        }
//    }
//}
//
//@Database(entities = arrayOf(User::class), version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao
//
//    companion object : SingletonHolder<AppDatabase, Context>({
//        Room.databaseBuilder(it.applicationContext,
//            AppDatabase::class.java, "Sample.db")
//            .build()
//    })
//}