namespace Lofka.Dotnet.Log4net
{
    using Common;
    using Common.Entites;
    using log4net.Core;
    /// <summary>
    /// Log对象转换
    /// </summary>
    public static class LogConvert
    {
        /// <summary>
        /// 将LoggingEvent 转换为LoggerInfo json字符串
        /// </summary>
        /// <param name="event">LoggingEvent</param>
        /// <returns></returns>
        public static LoggerInfo ToLoggerInfo(this LoggingEvent @event,string appName)
        {
            if (@event == null)
            {
                return null;
            }
            return  new LoggerInfo
            {
                Message = @event.RenderedMessage,
                TimeStamp = @event.TimeStamp.ToTimestamp(),
                Level = @event.Level.Name.ToUpper(),
                ThreadName = @event.ThreadName,
                LoggerName = @event.LoggerName,
                //原先以为log4net会对LocationInformation赋值，后来发现并没有😭，素以删除了Location属性
                //Location = @event.LocationInformation.ToLocation(),
                Throwable = @event.ExceptionObject.ToThrowable(),
                AppName= appName
            };

        }
        /// <summary>
        /// 将 log4net 的 LocationInfo 转换为LogLocation
        /// </summary>
        /// <param name="location">LocationInfo</param>
        /// <returns></returns>
        public static LogLocation ToLocation(this LocationInfo location) => location.ClassName !="?"
                ? new LogLocation
                {
                    ClassName = location.ClassName,
                    FileName = location.FileName,
                    Line = int.Parse(location.LineNumber),
                    MethodName = location.MethodName
                }
                : null;
    }
}
