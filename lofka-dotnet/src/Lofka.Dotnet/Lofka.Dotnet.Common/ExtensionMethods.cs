using System;

namespace Lofka.Dotnet.Common
{
    using Entites;
    using System.Diagnostics;

    public static class ExtensionMethods
    {
        private static readonly DateTime utcZero = TimeZoneInfo.ConvertTime(new DateTime(1970, 1, 1), TimeZoneInfo.Local);
        /// <summary>
        /// 将时间转换为时间戳
        /// </summary>
        /// <param name="time">要被转换的试试加</param>
        /// <returns></returns>
        public static double ToTimestamp(this DateTime time) => time > utcZero ? (time - utcZero).TotalMilliseconds : 0;

        /// <summary>
        /// 将Exception 转换为LogThrowable对象
        /// </summary>
        /// <param name="ex"></param>
        /// <returns></returns>
        public static LogThrowable ToThrowable(this Exception ex)
        {
            if (ex != null)
            {
                var stackTrace = new StackTrace(ex,true);
                var throwable = new LogThrowable
                {
                    Message = ex.Message
                };
                var frames = stackTrace.GetFrames();
                if (frames != null && frames.Length > 0)
                {
                    foreach (var item in frames)
                    {
                        throwable.StackTrace.Add(new LogLocation
                        {
                            ClassName = item.GetMethod().ReflectedType.FullName,
                            FileName = item.GetFileName(),
                            Line = item.GetFileLineNumber(),
                            MethodName = item.GetMethod().Name
                        });
                    }
                }
                return throwable;
            }
            return null;
        }
    }
}
