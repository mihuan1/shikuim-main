package com.shiku.push;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.shiku.push.service.HWPushService;

import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.MsgNotice;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=PushApplication.class)
public class PushTest {

	@Test
	public void testHWPush() {
		MsgNotice notice =new MsgNotice();
		notice.setText("333333333333");
		notice.setTitle("44444444444");
		try {
			HWPushService.sendPushMessage(notice,notice.getFileName(),
					"0865217039424357300002971000CN01");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
