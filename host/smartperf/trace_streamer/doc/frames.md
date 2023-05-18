# 帧渲染数据解析的逻辑
帧的解析分为应用帧的解析和渲染帧的业务解析。  
## 应用帧
应用帧解析主要是进行应用帧业务的开始和结束，并对其帧编号进行记录，以便在RS的业务中进行匹配（RenderService简称为RS）。  
应用帧会有下面一些调用栈：  
H:ReceiveVsync  
H:OnVsyncEvent  
H:MarshRSTransactionData  
后面两个调用栈嵌套在H:ReceiveVsync内。  
特别地，当一个栈没有H:MarshRSTransactionData事件时，是无效的帧。  
H:MarshRSTransactionData内会包含应用的帧标号，和线程号。  
## 渲染帧
渲染帧会有下面一些调用栈：  
H:ReceiveVsync  
H:RSMainThread::OnVsync  
H:RSMainThread::ProcessCommandUni  
后面两个调用栈嵌套在H:ReceiveVsync内。  
特别地：当渲染帧不包含H:RSMainThread::ProcessCommandUni时，为无效的渲染帧。  
H:RSMainThread::ProcessCommandUni内包含被渲染的帧编号，所属的线程号。  
## GPU渲染时长
和渲染帧同步并发的，是渲染线程的gpu信息，在事件中是H:M: Frame queued事件。  
只有当H:M: Frame queued事件的开始时间在渲染帧的H:ReceiveVsync时间的开始到结束的时间范围时，将该H:M: Frame queued事件和H:ReceiveVsync做关联，两者构成一个完整的渲染事件（也可能没有H:M: Frame queued事件）。  
而应用的渲染帧编号，和渲染线程中H:RSMainThread::ProcessCommandUni事件所携带的被渲染的帧编号，所属的线程号关联之后，形成应用帧的一个完整渲染事件。  
## 关于帧卡顿的定义
关于帧卡顿的定义：帧的实际渲染时间晚于期望被渲染的时间，认为帧卡顿。数据库中标识为1，否则为0。  
期望帧不做任何标识，为默认的255(max u8)，导出db时为空。  
特别地：以上事件发出的线程，必须是进程中的主线程，即线程号和进程号一致，否则，不做处理。  
## 关于无效帧的定义
如果应用帧不包含FrameNum，则其为无效帧。  
无效帧的标识为2。  
当帧为无效帧时，会同时标识其实际帧和期望帧为无效帧。  