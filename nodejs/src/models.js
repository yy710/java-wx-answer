function model(table, columns, options = {}) {
  const byProp = {};
  const byColumn = {};
  for (const [prop, column] of Object.entries(columns)) {
    byProp[prop] = column;
    byColumn[column] = prop;
  }
  return {
    table,
    columns: byProp,
    byColumn,
    idProp: options.idProp || 'id',
    idColumn: options.idColumn || byProp[options.idProp || 'id'] || 'id',
    keys: options.keys || null,
    defaultOrder: options.defaultOrder || null,
    likeFields: options.likeFields || ['title', 'name', 'nickName', 'username', 'mobile']
  };
}

const models = {
  agreement: model('yk_agreement', { id: 'id', type: 'type', content: 'content', title: 'title', url: 'url' }),
  appNotice: model('yk_app_notice', {
    id: 'id',
    note: 'note',
    pic: 'pic',
    sort: 'sort',
    title: 'title',
    brief: 'brief',
    type: 'type',
    createTime: 'create_time',
    updateTime: 'update_time'
  }, { defaultOrder: 'sort ASC' }),
  area: model('yk_area', {
    areaId: 'area_id',
    areaName: 'area_name',
    parentId: 'parent_id',
    level: 'level'
  }, { idProp: 'areaId', idColumn: 'area_id', defaultOrder: 'area_id ASC' }),
  category: model('category', {
    id: 'id',
    title: 'title',
    pic: 'pic',
    descr: 'descr',
    seq: 'seq',
    status: 'status',
    createTime: 'create_time',
    updateTime: 'update_time'
  }, { defaultOrder: 'seq ASC' }),
  customServer: model('yk_custom_server', {
    id: 'id',
    phone: 'phone',
    type: 'type',
    pic: 'pic',
    appId: 'app_id',
    name: 'name'
  }),
  feedback: model('yk_feedback', {
    id: 'id',
    userId: 'user_id',
    problemType: 'problem_type',
    handleFlag: 'handle_flag',
    content: 'content',
    imgUrl: 'img_url',
    handelResponse: 'handel_response',
    createTime: 'create_time',
    updateTime: 'update_time'
  }, { defaultOrder: 'id DESC', likeFields: ['nickName', 'content'] }),
  introduce: model('introduce', {
    id: 'id',
    title: 'title',
    author: 'author',
    descr: 'descr',
    viewNum: 'view_num',
    seq: 'seq',
    status: 'status',
    createTime: 'create_time'
  }, { defaultOrder: 'seq ASC' }),
  inviteSet: model('invite_set', {
    id: 'id',
    rewardAmount: 'reward_amount',
    rewardLimit: 'reward_limit',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  payment: model('payment', {
    id: 'id',
    title: 'title',
    descr: 'descr',
    status: 'status',
    deleteFlag: 'delete_flag',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  rewardSet: model('reward_set', {
    id: 'id',
    type: 'type',
    rewardLimit: 'reward_limit',
    firstFlag: 'first_flag',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  riskWarning: model('risk_warning', {
    id: 'id',
    title: 'title',
    rewardAmount: 'reward_amount',
    categoryId: 'category_id',
    status: 'status',
    seq: 'seq',
    viewNum: 'view_num',
    rewardNum: 'reward_num',
    timeMin: 'time_min',
    descr: 'descr',
    createTime: 'create_time',
    updateTime: 'update_time'
  }, { defaultOrder: 'seq ASC' }),
  riskWarningRecord: model('risk_warning_record', {
    riskWarnId: 'risk_warn_id',
    userId: 'user_id',
    createTime: 'create_time'
  }, { keys: ['riskWarnId', 'userId'], defaultOrder: 'create_time DESC' }),
  signActivity: model('sign_activity', {
    id: 'id',
    title: 'title',
    descr: 'descr',
    startTime: 'start_time',
    endTime: 'end_time',
    rewardAmount: 'reward_amount',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  sysLogInfo: model('sys_log_info', {
    id: 'id',
    userId: 'user_id',
    username: 'username',
    operation: 'operation',
    method: 'method',
    params: 'params',
    time: 'time',
    ip: 'ip',
    createTime: 'create_time',
    type: 'type'
  }, { defaultOrder: 'create_time DESC' }),
  sysPermission: model('sys_permission', {
    id: 'id',
    pid: 'pid',
    name: 'name',
    permission: 'permission',
    icon: 'icon',
    type: 'type',
    uri: 'uri',
    status: 'status',
    sort: 'sort',
    code: 'code',
    createTime: 'create_time',
    updateTime: 'update_time',
    deleteFlag: 'delete_flag'
  }, { defaultOrder: 'sort ASC' }),
  sysRole: model('sys_role', {
    id: 'id',
    code: 'code',
    name: 'name',
    deleteFlag: 'delete_flag',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  sysRolePermission: model('sys_role_permission', {
    roleId: 'role_id',
    permissionId: 'permission_id'
  }, { keys: ['roleId', 'permissionId'] }),
  sysUser: model('sys_user', {
    id: 'id',
    username: 'username',
    password: 'password',
    icon: 'icon',
    nickName: 'nick_name',
    note: 'note',
    createTime: 'create_time',
    updateTime: 'update_time',
    status: 'status',
    deleteFlag: 'delete_flag'
  }, { likeFields: ['username', 'nickName'] }),
  sysUserRole: model('sys_user_role', {
    userId: 'user_id',
    roleId: 'role_id'
  }, { keys: ['userId', 'roleId'] }),
  ticketActivity: model('ticket_activity', {
    id: 'id',
    title: 'title',
    startTime: 'start_time',
    endTime: 'end_time',
    startTicketTime: 'start_ticket_time',
    endTicketTime: 'end_ticket_time',
    ticketLimit: 'ticket_limit',
    ticketUserNum: 'ticket_user_num',
    ticketTotal: 'ticket_total',
    viewNum: 'view_num',
    ticketUserMultiple: 'ticket_user_multiple',
    ticketMultiple: 'ticket_multiple',
    descr: 'descr',
    viewMultiple: 'view_multiple',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  ticketActivityVideo: model('ticket_activity_video', {
    ticketActivityId: 'ticket_activity_id',
    videoId: 'video_id',
    seq: 'seq',
    rewardAmount: 'reward_amount',
    minTime: 'min_time'
  }, { keys: ['ticketActivityId', 'videoId'], defaultOrder: 'seq ASC' }),
  ticketRecord: model('ticket_record', {
    id: 'id',
    userId: 'user_id',
    ticketActivityId: 'ticket_activity_id',
    ticketVideoId: 'ticket_video_id',
    ipAddr: 'ip_addr',
    createTime: 'create_time'
  }, { defaultOrder: 'id DESC' }),
  topic: model('topic', {
    id: 'id',
    title: 'title',
    rewardAmount: 'reward_amount',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  topicActivity: model('topic_activity', {
    id: 'id',
    title: 'title',
    limitNum: 'limit_num',
    startTime: 'start_time',
    endTime: 'end_time',
    viewNum: 'view_num',
    userNum: 'user_num',
    rewardExtra: 'reward_extra',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  topicItem: model('topic_item', {
    id: 'id',
    topicId: 'topic_id',
    title: 'title',
    seq: 'seq',
    answerFlag: 'answer_flag'
  }, { defaultOrder: 'seq ASC' }),
  topicLine: model('topic_line', {
    id: 'id',
    topicActivityId: 'topic_activity_id',
    title: 'title',
    seq: 'seq',
    lightSeq: 'light_seq',
    status: 'status',
    topicNum: 'topic_num',
    descr: 'descr'
  }, { defaultOrder: 'seq ASC' }),
  topicRecord: model('topic_record', {
    id: 'id',
    userId: 'user_id',
    topicActivityId: 'topic_activity_id',
    topicActivityTitle: 'topic_activity_title',
    topicLineId: 'topic_line_id',
    topicLineTitle: 'topic_line_title',
    rewardAmount: 'reward_amount',
    usedTime: 'used_time',
    rightNum: 'right_num',
    totalNum: 'total_num',
    createTime: 'create_time'
  }, { defaultOrder: 'id DESC', likeFields: ['topicActivityTitle', 'topicLineTitle', 'nickName'] }),
  topicRecordActivity: model('topic_record_activity', {
    id: 'id',
    topicActivityId: 'topic_activity_id',
    userId: 'user_id',
    totalRewardAmount: 'total_reward_amount'
  }),
  topicRecordSingle: model('topic_record_single', {
    id: 'id',
    topicId: 'topic_id',
    topicTitle: 'topic_title',
    userId: 'user_id',
    rightFlag: 'right_flag',
    rewardAmount: 'reward_amount',
    createTime: 'create_time'
  }, { defaultOrder: 'id DESC', likeFields: ['topicTitle', 'nickName'] }),
  topicRecordSingleItem: model('topic_record_single_item', {
    topicRecordSingleId: 'topic_record_single_id',
    topicItemId: 'topic_item_id',
    topicItemTitle: 'topic_item_title',
    checkFlag: 'check_flag',
    answerFlag: 'answer_flag'
  }, { keys: ['topicRecordSingleId', 'topicItemId'] }),
  topicRecordTopic: model('topic_record_topic', {
    id: 'id',
    topicRecordId: 'topic_record_id',
    topicId: 'topic_id',
    userId: 'user_id',
    topicTitle: 'topic_title',
    rewardAmount: 'reward_amount',
    answerFlag: 'answer_flag'
  }),
  topicRecordTopicItem: model('topic_record_topic_item', {
    topicRecordTopicId: 'topic_record_topic_id',
    topicItemId: 'topic_item_id',
    topicItemTitle: 'topic_item_title',
    checkFlag: 'check_flag',
    answerFlag: 'answer_flag'
  }, { keys: ['topicRecordTopicId', 'topicItemId'] }),
  user: model('yk_user', {
    id: 'id',
    nickName: 'nick_name',
    mobile: 'mobile',
    pic: 'pic',
    openId: 'open_id',
    status: 'status',
    createTime: 'create_time',
    updateTime: 'update_time',
    deleteFlag: 'delete_flag',
    parentId: 'parent_id'
  }, { likeFields: ['nickName', 'mobile'] }),
  userWallet: model('user_wallet', {
    id: 'id',
    userId: 'user_id',
    type: 'type',
    amount: 'amount',
    version: 'version'
  }),
  userWalletRecord: model('user_wallet_record', {
    id: 'id',
    walletId: 'wallet_id',
    changeAmount: 'change_amount',
    afterAmount: 'after_amount',
    eventId: 'event_id',
    eventType: 'event_type',
    status: 'status',
    createTime: 'create_time'
  }, { defaultOrder: 'id DESC', likeFields: ['nickName'] }),
  video: model('video', {
    id: 'id',
    title: 'title',
    descr: 'descr',
    status: 'status',
    urlVideo: 'url_video',
    urlPic: 'url_pic',
    seq: 'seq',
    ticketTotal: 'ticket_total',
    createTime: 'create_time',
    updateTime: 'update_time'
  }, { defaultOrder: 'seq ASC' }),
  videoActivity: model('video_activity', {
    id: 'id',
    title: 'title',
    rewardLimit: 'reward_limit',
    descr: 'descr',
    startTime: 'start_time',
    endTime: 'end_time',
    createTime: 'create_time',
    updateTime: 'update_time'
  }),
  videoActivityVideo: model('video_activity_video', {
    videoActivityId: 'video_activity_id',
    videoId: 'video_id',
    seq: 'seq',
    rewardAmount: 'reward_amount',
    minTime: 'min_time'
  }, { keys: ['videoActivityId', 'videoId'], defaultOrder: 'seq ASC' })
};

const adminResources = {
  agreement: 'agreement',
  appNotice: 'appNotice',
  category: 'category',
  customerServer: 'customServer',
  introduce: 'introduce',
  inviteSet: 'inviteSet',
  payment: 'payment',
  rewardSet: 'rewardSet',
  riskWarning: 'riskWarning',
  signActivity: 'signActivity',
  ticketActivity: 'ticketActivity',
  topic: 'topic',
  topicActivity: 'topicActivity',
  topicLine: 'topicLine',
  topicRecordTopic: 'topicRecordTopic',
  user: 'user',
  video: 'video',
  videoActivity: 'videoActivity'
};

const walletEventLabels = {
  1: '风险提示阅读',
  2: '答题奖励',
  3: '扫码支付',
  4: '日常签到',
  5: '邀请新人',
  6: '积分视频奖励',
  7: '趣味答题',
  8: '投票视频奖励'
};

const WalletEvent = {
  RISK_READ: 1,
  TOPIC_REWARD: 2,
  SCAN_PAY: 3,
  DAILY_SIGN: 4,
  INVITE_REWARD: 5,
  VIDEO: 6,
  SINGLE_TOPIC_REWARD: 7,
  TICKET_VIDEO: 8
};

module.exports = {
  models,
  adminResources,
  walletEventLabels,
  WalletEvent
};
