package cn.xyz.mianshi.service;

import javax.annotation.Resource;

import cn.xyz.mianshi.service.impl.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;

import cn.xyz.commons.autoconfigure.KApplicationProperties;
import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.PushConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.SmsConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.support.jedis.RedisCRUD;
import cn.xyz.mianshi.lable.LabelManagerImpl;
import cn.xyz.mianshi.lable.UserLabelManagerImpl;
import cn.xyz.mianshi.lable.UserLabelRepositoryImpl;
import cn.xyz.mianshi.opensdk.OpenAccountManageImpl;
import cn.xyz.mianshi.opensdk.OpenAppManageImpl;
import cn.xyz.mianshi.opensdk.OpenCheckLogManageImpl;
import cn.xyz.mianshi.opensdk.OpenWebManageImpl;
import cn.xyz.repository.mongo.AdminRepositoryImpl;
import cn.xyz.repository.mongo.CompanyRepositoryImpl;
import cn.xyz.repository.mongo.CustomerRepositoryImpl;
import cn.xyz.repository.mongo.DepartmentRepositoryImpl;
import cn.xyz.repository.mongo.EmployeeRepositoryImpl;
import cn.xyz.repository.mongo.FriendsRepositoryImpl;
import cn.xyz.repository.mongo.MsgCommentRepositoryImpl;
import cn.xyz.repository.mongo.MsgGiftRepositoryImpl;
import cn.xyz.repository.mongo.MsgListRepositoryImpl;
import cn.xyz.repository.mongo.MsgPraiseRepositoryImpl;
import cn.xyz.repository.mongo.MsgRepositoryImpl;
import cn.xyz.repository.mongo.UserRepositoryImpl;
import cn.xyz.service.KSMSServiceImpl;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.RedisServiceImpl;

/**
* @Description: TODO(单例类管理)
* @author lidaye
* @date 2018年7月21日
*/
@Service
public class LocalSpringBeanManager {

	@Resource(name = "dsForRW")
	private Datastore dsForRW;
	public Datastore getDatastore() {
		return dsForRW;
	}
	@Autowired
	protected Morphia morphia;
	public Morphia getMorphia() {
		return morphia;
	}


	@Resource(name="mongoClient")
	private MongoClient mongoClient;
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	@Autowired(required=false)
	private Datastore dsForTigase;

	public Datastore getTigaseDatastore() {
		return dsForTigase;
	}

	@Autowired(required=false)
	private Datastore dsForRoom;
	public Datastore getImRoomDatastore() {
		return dsForRoom;
	}
	@Autowired(required=false)
	private MongoClient tigMongoClient;
	public MongoClient getTigMongoClient() {
		return tigMongoClient;
	}
	@Autowired(required=false)
	private MongoClient imRoomMongoClient;
	public MongoClient getImRoomMongoClient() {
		return imRoomMongoClient;
	}



	@Autowired(required=false)
	private PushConfig pushConfig;

	public PushConfig getPushConfig() {
		return pushConfig;
	}

	@Autowired(required=false)
	private XMPPConfig xmppConfig;
	public XMPPConfig getXMPPConfig() {
		return xmppConfig;
	}

	@Autowired(required=false)
	private SmsConfig smsConfig;
	public SmsConfig getSmsConfig() {
		return smsConfig;
	}

    @Autowired(required=false)
    private KApplicationProperties.AgoraConfig agoraConfig;
    public KApplicationProperties.AgoraConfig getAgoraConfig() {
        return agoraConfig;
    }

	@Autowired(required=false)
	private RedisCRUD redisCRUD;

	public RedisCRUD getRedisCRUD() {
		return redisCRUD;
	}
	@Autowired(required=false)
    private RedissonClient redissonClient;
	public RedissonClient getRedissonClient() {
		return redissonClient;
	}

	@Autowired(required=false)
	private KApplicationProperties config;
	public KApplicationProperties getApplicationConfig() {
		return config;
	}
	@Autowired(required=false)
	private AppConfig appConfig;
	public AppConfig getAppConfig() {
		return appConfig;
	}

	@Autowired(required=false)
	private KSMSServiceImpl smsService;

	public KSMSServiceImpl getSMSService() {
		return smsService;
	}
	@Autowired(required=false)
	private KXMPPServiceImpl xmppService;
	public KXMPPServiceImpl getXmppService(){
		return xmppService;
	}
	@Autowired(required=false)
	private RedisServiceImpl redisService;
	public RedisServiceImpl getRedisService(){
		return redisService;
	}

	@Autowired
	private AdminManagerImpl adminManager;

	public AdminManagerImpl getAdminManager() {
		return adminManager;
	}

	@Autowired
	private UserManagerImpl userManager;
	public UserManagerImpl getUserManager() {
		return userManager;
	}

	@Autowired(required=false)
	private PersistentServiceImpl persistent;

	public PersistentServiceImpl getPersistent() {
		return persistent;
	}

	@Autowired
	private FriendsManagerImpl friendsManager;
	public FriendsManagerImpl getFriendsManager() {
		return friendsManager;
	}

	@Autowired
	private FriendGroupManagerImpl friendGroupManager;
	public FriendGroupManagerImpl getFriendGroupManager() {
		return friendGroupManager;
	}

	@Autowired
	private FriendsRepositoryImpl friendsRepository;
	public FriendsRepositoryImpl getFriendsRepository() {
		return friendsRepository;
	}

	@Autowired
	private RoomManagerImplForIM roomManager;

	public RoomManagerImplForIM getRoomManager() {
		return roomManager;
	}


	@Autowired
	private TigaseManagerImpl tigaseManager;
	public TigaseManagerImpl getTigaseManager() {
		return tigaseManager;
	}

	@Autowired
	private AddressBookManagerImpl addressBookManager;
	public AddressBookManagerImpl getAddressBookManger() {
		return addressBookManager;
	}

	@Autowired
	private CompanyManagerImpl companyManager;
	public CompanyManagerImpl getCompanyManager() {
		return companyManager;
	}

	@Autowired
	private ConsumeRecordManagerImpl consumeRecordManager;
	public ConsumeRecordManagerImpl getConsumeRecordManager() {
		return consumeRecordManager;
	}

	@Autowired
	private CustomerManagerImpl customerManager;
	public CustomerManagerImpl getCustomerManager() {
		return customerManager;
	}

	@Autowired
	private UserLabelManagerImpl userLabelManager;
	public UserLabelManagerImpl getUserLabelManager() {
		return userLabelManager;
	}

	@Autowired
	private LabelManagerImpl labelManager;
	public LabelManagerImpl getLabelManager() {
		return labelManager;
	}

	@Autowired
	private LiveRoomManagerImpl liveRoomManager;
	public LiveRoomManagerImpl getLiveRoomManager() {
		return liveRoomManager;
	}

	@Autowired
	private MsgCommentRepositoryImpl msgCommentRepository;
	public MsgCommentRepositoryImpl getMsgCommentRepository() {
		return msgCommentRepository;
	}

	@Autowired
	private MsgGiftRepositoryImpl msgGiftRepository;
	public MsgGiftRepositoryImpl getMsgGiftRepository() {
		return msgGiftRepository;
	}

	@Autowired
	private MsgRepositoryImpl msgRepository;
	public MsgRepositoryImpl getMsgRepository() {
		return msgRepository;
	}

	@Autowired
	private MsgPraiseRepositoryImpl msgPraiseRepository;
	public MsgPraiseRepositoryImpl getMsgPraiseRepository() {
		return msgPraiseRepository;
	}

	@Autowired
	private MsgListRepositoryImpl msgListRepository;
	public MsgListRepositoryImpl getMsgListRepository() {
		return msgListRepository;
	}

	@Autowired
	private RedPacketManagerImpl redPacketManager;
	public RedPacketManagerImpl getRedPacketManager() {
		return redPacketManager;
	}

	@Autowired
	private BankCardManagerImpl bankCardManager;
	public BankCardManagerImpl getBankCardManager() {
		return bankCardManager;
	}

	@Autowired
	private PaidMerchantManagerImpl paidMerchantManager;
	public PaidMerchantManagerImpl getPaidMerchantManager() {
		return paidMerchantManager;
	}

	@Autowired
	private VendorPayManagerImpl paidVendorPayManager;
	public VendorPayManagerImpl getVendorPayManager() {
		return paidVendorPayManager;
	}

	@Autowired
	private SkTransferManagerImpl skTransferManagerImpl;
	public SkTransferManagerImpl getSkTransferManagerImpl(){
		return skTransferManagerImpl;
	}

	@Autowired
	private UserLabelRepositoryImpl userLabelRepository;
	public UserLabelRepositoryImpl getUserLabelRepository() {
		return userLabelRepository;
	}


	@Autowired
	private AdminRepositoryImpl adminRepository;
	public AdminRepositoryImpl getAdminRepository() {
		return adminRepository;
	}

	@Autowired
	private UserRepositoryImpl userRepository;
	public UserRepositoryImpl getUserRepository() {
		return userRepository;
	}

	@Autowired
	private EmployeeRepositoryImpl employeeRepository;
	public EmployeeRepositoryImpl getEmployeeRepository() {
		return employeeRepository;
	}

	@Autowired
	private DepartmentRepositoryImpl departmentRepository;
	public DepartmentRepositoryImpl getDepartmentRepository() {
		return departmentRepository;
	}

	@Autowired
	private CompanyRepositoryImpl companyRepository;
	public CompanyRepositoryImpl getCompanyRepository() {
		return companyRepository;
	}

	@Autowired
	private CustomerRepositoryImpl customerRepository;
	public CustomerRepositoryImpl getCustomerRepository() {
		return customerRepository;
	}

	@Autowired
	private ErrorMessageManageImpl errorMessageManage;
	public ErrorMessageManageImpl getErrorMessageManage() {
		return errorMessageManage;
	}

	@Autowired
	private MPServiceImpl mpManager;
	public MPServiceImpl getMpManager() {
		return mpManager;
	}

	@Autowired
	private ReportManagerImpl reportManager;
	public ReportManagerImpl getReportManager() {
		return reportManager;
	}

	@Autowired
	private MenuManagerImpl menuManager;
	public MenuManagerImpl getMenuManager(){
		return menuManager;
	}

	@Autowired
	private OpenAccountManageImpl openAccountManage;
	public OpenAccountManageImpl getOpenAccountManage(){
		return openAccountManage;
	}

	@Autowired
	private OpenAppManageImpl openAppManageImpl;
	public OpenAppManageImpl getOpenAppManage(){
		return openAppManageImpl;
	}

	@Autowired
	private OpenCheckLogManageImpl openCheckLogManage;
	public OpenCheckLogManageImpl getOpenCheckLogManage(){
		return openCheckLogManage;
	}

	@Autowired
	private OpenWebManageImpl openWebAppManage;
	public OpenWebManageImpl getOpenWebAppManage(){
		return openWebAppManage;
	}


	@Autowired
	private RoleManagerImpl roleManager;
	public RoleManagerImpl getRoleManager(){
		return roleManager;
	}

	@Autowired
	private MusicManagerImpl musicManager;
	public MusicManagerImpl getMusicManager(){
		return musicManager;
	}

	@Autowired
	private PressureTestManagerImpl pressureTestManager;
	public PressureTestManagerImpl getPressureTestManager(){
		return pressureTestManager;
	}

	@Autowired
	private AgoraManagerImpl agoraManager;
	public AgoraManagerImpl getAgoraManager(){
		return agoraManager;
	}

    @Autowired
    private FriendsterWebsiteManagerImpl friendsterWebsiteManager;
    public FriendsterWebsiteManagerImpl getFriendsterWebsiteManager(){
        return friendsterWebsiteManager;
    }

}

