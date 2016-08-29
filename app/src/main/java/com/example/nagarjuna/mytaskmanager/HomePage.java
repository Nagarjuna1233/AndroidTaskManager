package com.example.nagarjuna.mytaskmanager;

import java.util.ArrayList;
import java.util.List;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;



public class HomePage extends AppCompatActivity {

    ArrayList<String> apps=new ArrayList<String>();
    ArrayList<Drawable> icons=new ArrayList<Drawable>();
    ListView listView ;
    ImageView img;
    ApplicationAdapter app;
    ActivityManager activityManager;
    List<RunningTaskInfo> allapps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        listView = (ListView) findViewById(R.id.list);
        PackageManager pack=this.getPackageManager();


        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        new LoadApplications().execute();



    }

    //load applications

    private class LoadApplications extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            allapps= activityManager.getRunningTasks(Integer.MAX_VALUE);
            app= new ApplicationAdapter(HomePage.this,R.layout.snippet_list_row,allapps);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            listView.setAdapter(app);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            //listView.setAdapter(app);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    //coustome list adapter
    public class ApplicationAdapter extends ArrayAdapter<RunningTaskInfo> {
        List<RunningTaskInfo> appsList = null;
        private Context context;
        private PackageManager packageManager;
        public   String  apn;

        public ApplicationAdapter(Context context, int textViewResourceId,
                                  List<RunningTaskInfo> appsList) {
            super(context, textViewResourceId, appsList);
            this.context = context;
            this.appsList = appsList;
            packageManager = context.getPackageManager();
        }

        @Override
        public int getCount() {
            return ((null != appsList) ? appsList.size() : 0);
        }

        @Override
        public RunningTaskInfo getItem(int position) {
            return ((null != appsList) ? appsList.get(position) : null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Drawable dic;
            if (null == view) {
                LayoutInflater layoutInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.snippet_list_row, null);
            }

            RunningTaskInfo applicationInfo = appsList.get(position);
            if (null != applicationInfo) {
                TextView appName = (TextView) view.findViewById(R.id.app_name);
                ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);
                TextView memory_size = (TextView) view.findViewById(R.id.memory);
                try {
                    String name=applicationInfo.topActivity.getPackageName();
                    dic=packageManager.getApplicationIcon(name);
                    apn= (String)    packageManager.getApplicationLabel(packageManager.getApplicationInfo(name,PackageManager.GET_META_DATA));
                    int[] id={applicationInfo.id};
                    Debug.MemoryInfo[] minfo=activityManager.getProcessMemoryInfo(id);
                    appName.setText(apn);

                    long d=minfo[0].getTotalPss()+minfo[0].getTotalPrivateDirty()+minfo[0].getTotalSharedDirty();
                    memory_size.setText( "Process Usage Memory: "+String.valueOf(d)+" Kb");
                    iconview.setImageDrawable(dic);
                    view.setOnClickListener(new ClickListener(applicationInfo.id,name,apn,position));

                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }


            }

            return view;
        }
        public class ClickListener implements View.OnClickListener{
            int pid;
            String ap_name;
            String ap_pname;
            int list_position;
            public ClickListener(int pid,String ap_pname,String ap_name,int list_position){

                this.pid=pid;
                this.ap_name=ap_name;
                this.ap_pname=ap_pname;
                this.list_position=list_position;
            }
            public void onClick(View v) {


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomePage.this);



                alertDialogBuilder.setTitle(HomePage.this.getTitle()+ " decision");

                alertDialogBuilder.setMessage("Are you sure kill the task "+ap_name+"?");

                // set positive button: Yes message

                alertDialogBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,int id) {
                        //android.os.Process.sendSignal(pid, android.os.Process.SIGNAL_KILL);
                        android.os.Process.killProcess(pid);
                        Context con =getApplicationContext();

                        if(ap_pname.equals(con.getPackageName()))
                            finish();
                        //activityManager.restartPackage(ap_pname);
                        //activityManager.killBackgroundProcesses(ap_pname);
                        appsList.remove(list_position);
                        app.notifyDataSetChanged();

                    }

                });
                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        // cancel the alert box and put a Toast to the user

                        dialog.cancel();



                    }

                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                // show alert

                alertDialog.show();


            }




            // new LoadApplications().execute();
            // allapps= activityManager.getRunningTasks(Integer.MAX_VALUE);
            //Toast.makeText(getApplicationContext(),"Killed The Task "+ap_name,Toast.LENGTH_LONG).show();
        }
    }


    protected void onDestroy(){


        super.onDestroy();
    }
}
