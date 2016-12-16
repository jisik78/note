package com.example.administrator.note;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DbManager.DaoConfig config;
    private DbManager dbManager;
    private ListView mlv;
    private MyAdapter<Note> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化数据库参数
        initDb();

        mlv = (ListView) findViewById(R.id.lv_show);
        //注册上下文菜单
        registerForContextMenu(mlv);

        adapter = new MyAdapter<>(this);
        try {
            List<Note> notes = getNotesFromDb();
            if (notes != null) {
                adapter.setDatas(notes);
                mlv.setAdapter(adapter);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    private List<Note> getNotesFromDb() throws DbException {

        return dbManager.selector(Note.class).findAll();
    }

    private void initDb() {

        config = new DbManager.DaoConfig();
        config.setDbName("my_note")
                .setDbDir(new File("data/data/com.example.administrator.note/my_db"));
        //获取db管理者
        dbManager = x.getDb(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //填充menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        //searchView
        MenuItem item = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) item.getActionView();
        //Query 查询
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override //文本提交监听
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override  //文本改变监听
            public boolean onQueryTextChange(String newText) {

                adapter.getDatas().clear();
                adapter.notifyDataSetChanged();
                try {
                    WhereBuilder where = WhereBuilder.b("title", "like", "%" + newText + "%");
                    WhereBuilder where2 = WhereBuilder.b("text", "like", "%" + newText + "%");
                    List<Note> notes = dbManager.selector(Note.class).where(where).or(where2).findAll();
                    adapter.setDatas(notes);
                    adapter.notifyDataSetChanged();

                } catch (DbException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add:
                //添加一条数据
                addNote();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //获取的是普通的View信息
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        //强转成listview的信息
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        //通过listview的信息类获取当前点击的position
        int position = adapterContextMenuInfo.position;

        switch (item.getItemId()) {
            case R.id.item_delete_menu_context:
                //删除adapter的datas
                int id = adapter.getDatas().get(position).getId();
                adapter.getDatas().remove(position);
                //删除数据库的数据
                WhereBuilder where = WhereBuilder.b("id", "=", id);

                try {
                    dbManager.delete(Note.class, where);
                } catch (DbException e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();

                break;

            case R.id.item_update_menu_context:
                Note note = adapter.getDatas().get(position);
                updateNote(note);

                break;
        }

        return super.onContextItemSelected(item);
    }

    private void addNote() {

        //弹出对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_add_note, null);

        final EditText et_title = (EditText) layout.findViewById(R.id.et_title);
        final EditText et_text = (EditText) layout.findViewById(R.id.et_text);

        AlertDialog alertDialog = builder.setTitle("添加数据")
                .setIcon(R.mipmap.ic_launcher)
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (TextUtils.isEmpty(et_title.getText().toString()) || TextUtils.isEmpty(et_text.getText().toString())) {

                            Toast.makeText(MainActivity.this, "你输入的标题或者内容不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            Note note = new Note();
                            note.setText(et_text.getText().toString().trim());
                            note.setTitle(et_title.getText().toString().trim());
                            try {
                                dbManager.save(note);
                                List<Note> notes =  getNotesFromDb();
                                adapter.setDatas(notes);
                                adapter.notifyDataSetChanged();

                            } catch (DbException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();

        alertDialog.show();

    }

    private void updateNote(Note note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_update_note, null);

        final EditText et_title = (EditText) layout.findViewById(R.id.et_title_update);
        final EditText et_text = (EditText) layout.findViewById(R.id.et_text_update);
        final WhereBuilder where = WhereBuilder.b("title", "=", note.getTitle());

        et_title.setText(note.getTitle());
        et_text.setText(note.getText());

        AlertDialog dialog = builder.setTitle("修改数据")
                .setIcon(R.mipmap.ic_launcher)
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (TextUtils.isEmpty(et_title.getText().toString()) || TextUtils.isEmpty(et_text.getText().toString())) {

                            Toast.makeText(MainActivity.this, "你输入的标题或者内容不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            String newtitle = et_title.getText().toString().trim();
                            String newtext = et_text.getText().toString().trim();

                            KeyValue settitle = new KeyValue("title", newtitle);
                            KeyValue settext = new KeyValue("text", newtext);

                            try {
                                dbManager.update(Note.class, where, settitle, settext);
                                List<Note> notes = getNotesFromDb();
                                adapter.setDatas(notes);
                                adapter.notifyDataSetChanged();

                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();

        dialog.show();
    }





}