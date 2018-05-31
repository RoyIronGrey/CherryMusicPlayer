package com.example.cherrymusicplayer;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.cherrymusicplayer.PlayService.MusicBinder;

public class FragmentList extends Fragment implements OnClickListener {
	EditText editText;
	ListView musicListView;
	MusicBinder musicService;
	ImageButton flashButton;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof MusicBinder) {
				musicService = (MusicBinder) service;
				List<Music> musicList = musicService.getMusicList();
				flashList(musicList);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Intent service = new Intent(this.getActivity(), PlayService.class);
		this.getActivity().bindService(service, connection,
				this.getActivity().BIND_AUTO_CREATE);
		initViews();
		initEvents();
	}

	private void initViews() {
		editText = (EditText) this.getActivity().findViewById(
				R.id.list_edit_text);
		musicListView = (ListView) this.getActivity().findViewById(
				R.id.list_list_view);
		flashButton = (ImageButton) this.getActivity().findViewById(R.id.list_flash_img_btn);
	}

	private void initEvents() {
		musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MusicListItem musicItemView = (MusicListItem) view;
				musicService.playMusic(musicItemView.getMusic().getPostion());
			}
		});
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String str = editText.getText().toString();
				List<Music> musicList = musicService.getMusicList();
				List<Music> musicTempList = new ArrayList<Music>();
				for(int i=0;i<musicList.size();i++){
					if(musicList.get(i).getTITLE().contains(str)){
						musicTempList.add(musicList.get(i));
					}
				}
				flashList(musicTempList);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		flashButton.setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		this.getActivity().unbindService(connection);
		super.onDestroy();
	}

	public void flashList(List<Music> musicList) {
		MusicListAdapter musicListAdapter = new MusicListAdapter(
				this.getActivity(), musicList);
		musicListView.setAdapter(musicListAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.list_flash_img_btn:
			PageControler activity = (PageControler) this.getActivity();
			activity.flashDatabase();
			musicService.flashList();
			flashList(musicService.getMusicList());
			break;

		default:
			break;
		}
	}
}
