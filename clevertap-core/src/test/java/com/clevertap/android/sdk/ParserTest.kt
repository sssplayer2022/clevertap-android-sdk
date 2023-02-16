package com.clevertap.android.sdk

////ParserTestInputVariables TestStaticTesting // VarCacheTest
//@RunWith(RobolectricTestRunner::class)
//class ParserTest :BaseTestCase(){
//
//
//    @Test
//    fun test_parseVariablesForClasses(){
//        println("============before calling Parser.parseVariablesForClasses()")
//        checkVarCache()
//
//        Parser.parseVariables(ParserTestInputVariables())
//
//        println("============after calling Parser.parseVariablesForClasses()")
//        checkVarCache()
//        Assert.assertTrue(true)
//    }
//
//
//    @Test
//    fun test_parseVariables(){
//        println("============before calling Parser.parseVariablesForClasses()")
//        checkVarCache()
//
//        Parser.parseVariablesForClasses(ParserTestInputVariables::class.java)
//
//        println("============after calling Parser.parseVariablesForClasses()")
//        checkVarCache()
//        Assert.assertTrue(true)
//    }
//
//
//
//    private fun checkVarCache() {
//          println( "checkVarCache() called")
//          //println( "checkVarCache:  valuesFromClient:           ${VarCache.valuesFromClient}", )
//          println( "checkVarCache:  welcomeMsg:                 ${VarCache.getVariable<String?>("welcomeMsg")}", )
//          println( "checkVarCache:  isOptedForOffers:           ${VarCache.getVariable<Boolean?>("isOptedForOffers")}", )
//          println( "checkVarCache:  initialCoins:               ${VarCache.getVariable<Int?>("initialCoins")}", )
//          println( "checkVarCache:  correctGuessPercentage:     ${VarCache.getVariable<Float?>("correctGuessPercentage")}", )
//          println( "checkVariables: userConfigurableProps:      ${VarCache.getVariable<HashMap<String,Any>?>("userConfigurableProps")}" )
//          println( "checkVarCache:  aiName                      ${VarCache.getVariable<Array<String>?>("aiName")}", )
//    }
//
//
//    private fun checkVariables() {
//        println( "checkVariables() called")
//        println( "checkVariables: welcomeMsg: ${ParserTestInputVariables.welcomeMsg}" )
//        println( "checkVariables: isOptedForOffers: ${ParserTestInputVariables.isOptedForOffers}" )
//        println( "checkVariables: initialCoins: ${ParserTestInputVariables.initialCoins}" )
//        println( "checkVariables: correctGuessPercentage: ${ParserTestInputVariables.correctGuessPercentage}" )
//        println( "checkVariables: userConfigurableProps: ${ParserTestInputVariables.userConfigurableProps}" )
//        println( "checkVariables: aiName: ${(ParserTestInputVariables.aiName)}" )
//    }
//}
//
//@RunWith(RobolectricTestRunner::class)
//class VarCacheTest : BaseTestCase() {
//
//
//    @Test
//    fun test_getNameComponents() {
//        MyInputStream.testMyInPutStream()
//
//        Assert.assertTrue(true)
//    }
//
//
//
//}
//// a basic input stream class .
//// it will return a characters of a string(passed as input) one by one using the read() function
//// it mimics a video stream or file stream. those get downloaded from the internet and returned in bits with a delay
//class MyInputStream(private val str:String): InputStream() {
//    private var pos = 0
//    override fun read(): Int {
//        Thread.sleep(10)
//        if(pos < str.length ) {
//            val charCode = str[pos].code
//            pos++
//            return charCode
//        }
//        else return -1
//    }
//
//    companion object{
//        fun  testMyInPutStream(){
//            val stream = MyInputStream("Hello world")
//
//            println("====")
//            var x = stream.read()
//            while (x !=-1){
//                print(x.toChar())
//                x = stream.read()
//            }
//            println("\n====")
//
//        }
//    }
//}
//
//public  class TestStaticTesting {
//
//    public static class SecretHelper{
//        private static final ArrayList<String> secretList = new ArrayList<>();
//
//        public static void updateSecret(String secret){
//            if (!secretList.contains(secret)) {
//                secretList.clear();
//                secretList.add(secret);
//            }
//        }
//    }
//
//    public static class SecretHelper2{
//        private static final ArrayList<String> secretList = new ArrayList<>();
//
//        public static void updateSecret(String secret){
//            updateSecret(secret,secretList);
//        }
//
//        private static void updateSecret(String secret, ArrayList<String> sl ){
//            if (!sl.contains(secret)) {
//                sl.clear();
//                sl.add(secret);
//            }
//        }
//
//    }
//
//
//}


//public class ParserTestInputVariables {
//    @Variable public static String welcomeMsg = "Hi User";
//    @Variable   public static Boolean isOptedForOffers = true;
//    @Variable   public static Integer initialCoins = 35;
//    @Variable   public static Float correctGuessPercentage = 100.0F;
//    @Variable   public static HashMap<String,Object> userConfigurableProps = new HashMap<>();
//    @Variable   public static ArrayList<String> aiName = new ArrayList<>(Arrays.asList("don", "jason", "shiela", "may"));
//
//    static {
//        userConfigurableProps.put("numberOfGuesses", 3);
//        userConfigurableProps.put("difficultyLevel", 1.2F);
//        userConfigurableProps.put("ai_Gender","M");
//    }
//
//
//}


/*

class MyApp:Application() {
    companion object {
        const val appID = "app_4E1oVqnj8hvB2KmrAXRS5M6STH1fQGy3RvuhSQM73ew"
        const val production1 = "prod_be2EraPzw8kcAjdwXogxsXaEu3aSXABQUt8WDiYrShU"
        const val production2 = "prod_4A36JePcRSLpJMNnLDrqCfAI6LOuB7GDkMzwTOuzFso"
        const val production3 = "prod_ZkAXNrgpXEpW8oBm5tRk1sAG6A43Q4HqrpUCiN84HRg"
        const val development1 = "dev_GwGwcOisoaviTLiWwhWlbXEaAwOsFE1JUaUXYFOdnAY"
        const val development2 = "dev_6H120hDBNeRtBCyZIVkOmdwW1WkW1eX1VgyA1fkOYfM"
        const val development3 = "dev_ggCy2JYsAMpoZBVEnuDQwiaLfgfQsCckSazYWlSIgns"
        const val dataExport = "exp_QjCwMsk6beGm75Z2ba9VkBDKx73ZYxqgTktMAORTfuQ"
        const val contentReadOnly = "cro_1DpId00UGT1eirFbagmskpaLcy6Zg2oZa5AhFVZJKYU"
        const val contentReadWrite = "crw_O8O3ZAjLXoojRzXAfi9ACmtzAVGZH3KWBxnjRllJJcs"
    }

    override fun onCreate() {
        Leanplum.setLogLevel(Level.DEBUG)
        Leanplum.setVariantDebugInfoEnabled(true)
        super.onCreate()
        Leanplum.setApplicationContext(this)
        MyVars.detect()
        LeanplumActivityHelper.enableLifecycleCallbacks(this)

        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode(appID, development1);
        } else {
            Leanplum.setAppIdForProductionMode(appID, production1);
        }
        // Leanplum.setVariantDebugInfoEnabled(true)
        Leanplum.start(this)

    }
}

class MainActivity : AppCompatActivity() {
    private val binding by lazy { LayoutMainBinding.inflate(layoutInflater) }
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        with(binding){
            btEventLikedVideo.setOnClickListener { Leanplum.track("LIKED_VIDEO")
                Log.e(TAG, "onCreate: my user= ${Leanplum.getUserId()}", )
            }

            etEmail.setOnEditorActionListener { v, k, event ->
                Log.e(TAG, "etEmail.setOnKeyListener: key=$k, eventaction=${event} ", )
                if(k== EditorInfo.IME_ACTION_DONE){
                    val email = etEmail.text.toString()
                    Leanplum.setUserAttributes(mapOf("email" to email))
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            etGender.setOnEditorActionListener { v, k, event ->
                Log.e(TAG, "etGender.setOnKeyListener: key=$k, eventaction=${event} ", )

                if(k== EditorInfo.IME_ACTION_DONE){
                    val gender = etGender.text.toString()
                    Leanplum.setUserAttributes(mapOf("gender" to gender))
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            btCheckVars.setOnClickListener { checkVariables() }
            btUpdateVars.setOnClickListener { VarCache.sendContentIfChanged(true, false)}
        }


        MyVars.enableHandler(object : VariablesChangedCallback(){
            override fun variablesChanged() {
                checkVariables()
            }
        }
        )
    }


}



 public class MyVars {
    private static final String TAG = "MyVariables";


    public static  void detect() {
        Log.d("MyVariables", "detect() called");
        Parser.parseVariables(MyVars.class);
        Parser.parseVariablesForClasses(MyVars.class);
    }

    public  static  void enableHandler(VariablesChangedCallback callback) {
        Log.d("MyVariables", "enableHandler() called with: callback = " + callback);
        Leanplum.addVariablesChangedHandler(callback);
    }


}


*/