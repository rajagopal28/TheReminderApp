package com.example.remainderapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Toast;

class DataHelper {
	   private static final String DATABASE_NAME = "reminder.db";
	   private static final int DATABASE_VERSION = 1;
	   private static final String TABLE_NAME = "remindme";
	   private Context context;
	   private SQLiteDatabase db;
	  private SQLiteStatement insertStmt;
	   private static final String INSERT = "insert into " + TABLE_NAME + "(subject,date,description) values (?,?,?)";
	   public DataHelper(Context context) {
	      this.context = context;
	      OpenHelper openHelper = new OpenHelper(this.context);
	      try
	      {
	    	  this.db = openHelper.getWritableDatabase();
	          this.insertStmt = this.db.compileStatement(INSERT);
	      }
	      catch(Exception ex)
	      {
	    	  Toast.makeText(this.context,"Exception in constructor "+ex.toString(),Toast.LENGTH_LONG).show();
	    	  ex.printStackTrace();
	      }
	     
	   }
	   public void deleteOnDate(String date,String sub)
	   {
		   this.db.delete(TABLE_NAME, "date LIKE ? and subject LIKE '%"+sub+"%'", new String[]{date});
	   }
	   public long update(String sub,String date,String newDesc)
	   {
		   try
		   {
			   ContentValues myValues=new ContentValues();
			   myValues.put("description", newDesc);
			  return this.db.update(TABLE_NAME, myValues, "date LIKE ? and subject LIKE '%"+sub+"%'", new String[]{date});
		   }
		   catch(Exception ex)
		   {
			   Toast.makeText(context, "Exception Updating"+ex.toString(), Toast.LENGTH_LONG).show();
		   }
		   return 0;
	   }
	   public long insert(String subject,String date,String desc) {
		   
		   try
		   {
			   this.insertStmt.bindString(1, subject);
			     this.insertStmt.bindString(2, date);
			     this.insertStmt.bindString(3, desc);
			      return this.insertStmt.executeInsert();
		   }
		   catch(Exception ex)
		   {
			   ex.printStackTrace();
			   Toast.makeText(this.context,"Exception on Bind "+ex.toString(),Toast.LENGTH_LONG).show();
		   }
		   	return 0;
	   }
	   public void deleteAll() {
	      this.db.delete(TABLE_NAME, null, null);
	   }
	   public List<String> selectForDate(String date)
	   {
		     List<String> list = new ArrayList<String>();
		     try
		     {
		      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "subject","description" },
		        "date LIKE '%"+date+"'",null, null, null, "subject asc");
		      if (cursor.moveToFirst()) {
		         do {
		            list.add("Subject:"+cursor.getString(0)+" Description : " +cursor.getString(1));
		         } while (cursor.moveToNext());
		      }
		      if (cursor != null && !cursor.isClosed()) {
		         cursor.close();
		      }
		     }
		     catch(Exception ex)
		     {
		    	 ex.printStackTrace();
		    	 Toast.makeText(this.context,"Exception on Select One "+ex.toString(),Toast.LENGTH_LONG).show();
		     }
		      return list; 
	   }
	   public List<String> selectAll() {
	      List<String> list = new ArrayList<String>();
	      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "subject","date","description" },
	        null, null, null, null, "subject asc");
	      if (cursor.moveToFirst()) {
	         do {
	            list.add(cursor.getString(0)+" on: " +cursor.getString(1)+" desc:"+cursor.getString(2));
	         } while (cursor.moveToNext());
	      }
	      if (cursor != null && !cursor.isClosed()) {
	         cursor.close();
	      }
	      return list;
	   }
	   private static class OpenHelper extends SQLiteOpenHelper {
	      OpenHelper(Context context) {
	         super(context, DATABASE_NAME, null, DATABASE_VERSION);
	      }
	      @Override
	      public void onCreate(SQLiteDatabase db) {
	         db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, subject TEXT NOT NULL, date TEXT NOT NULL,description TEXT NOT NULL)");
	      }
	      @Override
	      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
	         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	         onCreate(db);
	      }
	   }
	}
public class RajuReminderAppActivity extends Activity implements OnItemSelectedListener, OnCheckedChangeListener {
	private Button edit,backBut,getRemind,saveChange;
	private Spinner monthlist;
	private boolean isInsert,isUpdate,isDelete;
	private boolean isDate,isMonth,isYear;
	private RadioGroup remindFor,updateOper;
    private DataHelper dh;
	Date d;
	String myDate;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.dh=new DataHelper(this);
        isInsert=isDate=true;
                
        d=new Date();
        String t;
        //Toast.makeText(this,"Today is:"+d.toString(),Toast.LENGTH_LONG).show();
        t = "" + d.getDate();
        ((EditText)findViewById(R.id.editText1)).setText(t);
        t = "" + (1900 + d.getYear());
        ((EditText)findViewById(R.id.editText2)).setText(t);
        
        setEditButtonListener();
        
        setRemindRadioListener();
        
        setMonthList();
        
        getTodaysRemind();
                		
    }
    public void setBackButtonListener()
    {
	        backBut.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					setContentView(R.layout.main);
					isDate=true;
					setMonthList();
					setEditButtonListener();
					setRemindRadioListener();
				}
			});
    }
    public void setOperRadioListener()
    {
		updateOper=(RadioGroup)findViewById(R.id.radioGroup2);
    	updateOper.setOnCheckedChangeListener(this);
    }
    public void setRemindRadioListener()
    {
        remindFor=(RadioGroup)findViewById(R.id.radioGroup1);
    	remindFor.setOnCheckedChangeListener(this);
    }
    public void setEditButtonListener()
    {
    	 edit=(Button)findViewById(R.id.update);
         edit.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setContentView(R.layout.nextpage);
 				setOperRadioListener();
 				isInsert=true;
 				backBut=(Button)findViewById(R.id.back1);
 		       setBackButtonListener();
 		        saveChange=(Button)(findViewById(R.id.buttonsave));
 		        saveChange.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						if(isInsert)
 						{
 							String input1,input2,input3;
 							input1=new String();
 							input2=new String();
 							input3=new String();
 							boolean allOk=false;
 							input1=((EditText)(findViewById(R.id.dateR))).getText().toString();
 							if(input1.length()!=0)
 							{
 								input2=((EditText)(findViewById(R.id.subject))).getText().toString();
 								if(input2.length()!=0)
 								{
 									input3=((EditText)(findViewById(R.id.editTextres))).getText().toString();
 									if(input3.length()!=0)
 									{
 										allOk=true;
 										//input=input1+" "+input2+" "+input3;
 									}
 								}
 							}
 							if(allOk)
 							{
 									//insertInToFile(myFileName,input);
 								dh.insert(input2,input1,input3);
 								myToast("Insertion Successful date:"+input2+" subject:"+input1+" description:"+input3);
 							}
 							
 						}
 						if(isUpdate)
 						{
 							String input1,input2,input3;
 							input1=new String();
 							input2=new String();
 							input3=new String();
 							boolean allOk=false;
 							input1=((EditText)(findViewById(R.id.dateR))).getText().toString();
 							if(input1.length()!=0)
 							{
 								input2=((EditText)(findViewById(R.id.subject))).getText().toString();
 								if(input2.length()!=0)
 								{
 									input3=((EditText)(findViewById(R.id.editTextres))).getText().toString();
 									if(input3.length()!=0)
 									{
 										allOk=true;
 									}
 								}
 							}// if end
 							if(allOk)
 							{
 								//updateFile(myFileName,pattern,input);
 								dh.update(input2,input1,input3);
 								myToast("Updation Successful date:"+input1+" subject:"+input2+" description:"+input3);
 							}
 						}
 						if(isDelete)
 						{
 							String input1,input2;
 							input1=new String();
 							input2=new String();
 							boolean allOk=false;
 							input1=((EditText)(findViewById(R.id.dateR))).getText().toString();
 							if(input1.length()!=0)
 							{
 								input2=((EditText)(findViewById(R.id.subject))).getText().toString();
 								if(input2.length()!=0)
 								{
 									allOk=true;
 								}
 							}// if end
 							if(allOk)
 							{
 								//deleteFromFile(myFileName,pattern);
 								dh.deleteOnDate(input1,input2);
 								myToast("Deletion Successful date:"+input1+" subject:"+input2);
 							}
 						}
 					}
 				});
 				
 			}
 		});
         getRemind=(Button)(findViewById(R.id.button1));
         getRemind.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				String t=new String();
 				
 				if(isDate)
 				{
 					 t=((EditText)(findViewById(R.id.editText1))).getText().toString();
 					 t+="/";
 					 t+=((Spinner)(findViewById(R.id.spinner1))).getSelectedItemPosition()+1;
 					 t+="/";
 					 t+=((EditText)(findViewById(R.id.editText2))).getText().toString();
 					 myDate=t;
 					//myToast("Checked Changed for Date:"+myDate);
 				}
 				if(isMonth)
 				{
 					 t="/";
 					 t+=((Spinner)(findViewById(R.id.spinner1))).getSelectedItemPosition()+1;
 					 t+="/";
 					 t+=((EditText)(findViewById(R.id.editText2))).getText().toString();
 					 myDate=t;
 					//myToast("Checked Changed for Month:"+myDate);
 				}
 				if(isYear)
 				{
 					 t="/";
 					 t+=((EditText)(findViewById(R.id.editText2))).getText().toString();
 					 myDate=t;
 					//myToast("Checked Changed for Year:"+myDate);
 				}
 				String Remind=getRemainder(myDate);
 				if(Remind!=null && Remind.length()>0)
 				{
 					Toast.makeText(v.getContext(), "Remainder:\n"+Remind, Toast.LENGTH_LONG).show();
 				}
 				else{
 					Toast.makeText(v.getContext(), "No Remainder Found On the Given Date", Toast.LENGTH_LONG).show();
 				}
 			}
 		});
    }
    public void setMonthList()
    {
        monthlist =(Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> monthadapter= ArrayAdapter.createFromResource(this,R.array.months_array ,  android.R.layout.simple_spinner_item);
        monthadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthlist.setAdapter(monthadapter);
        monthlist.setSelection(d.getMonth());
        monthlist.setOnItemSelectedListener(this);
    }
   
    public String getRemainder(String forDate)
    {
    	// date formant used is DD/MM/YYYY
    	List<String> res=dh.selectForDate(forDate);
    	StringBuilder out=new StringBuilder();
    	for(String rems : res)
    	{
    		out.append(rems+"\n");
    	}
    	return out.toString();
    }
    
  public void getTodaysRemind()
  {
		myDate=new String();
		myDate=d.getDate() + "/" +  (d.getMonth()+1) + "/" +(1900 + d.getYear());
		String remaindMe=getRemainder(myDate);
		//myToast("Today is:"+myDate);
		 if(remaindMe!=null && remaindMe.length()>0)
		 {
			 Toast.makeText(this, "Today's remainder:\n"+remaindMe, Toast.LENGTH_LONG).show();
		 }
  }
    public void myToast(String input)
    {
    	Toast.makeText(this, input, Toast.LENGTH_LONG).show();
    }
 
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		if(group==(updateOper))
		{
			switch(checkedId)
			{
			 case R.id.radioins:
			 {
				isInsert=true;
				isUpdate=isDelete=false;
				//myToast("Insert Selected");
				break;
			 }
			 case R.id.radioupd:
			 {
				isUpdate=true;
				isInsert=isDelete=false;
				//myToast("Update Selected");
				break;
			 }
			 case R.id.radiodel:
			 {
				isDelete=true;
				isUpdate=isInsert=false;
				//myToast("Delete Selected");
				break;
			 }
			}// switch
		}//if remind
		if(group==(remindFor))
		{
			
			switch(checkedId)
			{
			 case R.id.radio0:
			 {
				 isDate=true;
				 isMonth=isYear=false;
				 //myToast("For Date Selected");
				 break;
			 }
			 case R.id.radio1:
			 {
				 isMonth=true;
				 isYear=isDate=false;
				 //myToast(" For Month Selected");
				 break;
			 }
			 case R.id.radio2:
			 {
				 isYear=true;
				 isMonth=isDate=false;
				 //myToast("For Year Selected");
				 break;
			 }
			}//switch
		}//if update
	}

}