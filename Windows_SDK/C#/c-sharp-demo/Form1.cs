using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Threading;


namespace c_sharp_demo
{
    public partial class Form1 : Form
    {
        [StructLayout(LayoutKind.Sequential)]
        public struct Test
        {
            public int nPens;  // 笔状态
            public int nX;     // 笔x轴坐标
            public int nY;     // 笔y轴坐标
            public int nPenP;  // 笔压力
        }

        // 获取dll导出函数
        //[DllImport("usbDevModule.dll", CharSet = CharSet.Ansi/*, CallingConvention = CallingConvention.StdCall*/)]
        //public static extern IntPtr createObj();

        public delegate void DataCallBack(string pUsbData, ref Test test);

        [DllImport("usbDevModule.dll", CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
        [return: MarshalAs(UnmanagedType.I1)]
        public static extern bool _extern_openSpecUsbDevByPid(string[] str, int nSize, DataCallBack ps);

        [DllImport("usbDevModule.dll", CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
        public static extern bool _extern_CloseUsbDev();


        // 新增设备状态消息回调
        public delegate DevStatusCallBack DevStatusCallBack(int nDevstatus, IntPtr context);
        [DllImport("usbDevModule.dll", CharSet = CharSet.Ansi, CallingConvention = CallingConvention.Cdecl)]
        public static extern DevStatusCallBack _extern_setDevStatusHandler(DevStatusCallBack statusCallback, IntPtr pContext);

        /*[UnmanagedFunctionPointer(System.Runtime.InteropServices.CallingConvention.ThisCall)]
        private delegate bool fun_handler([In] IntPtr @this, [In] char[][] p, [In] int nSize, [In] IntPtr ptr);
        private delegate bool fun_handler2([In] IntPtr @this);
        private delegate int fun_handler_test([In] IntPtr p, string str);
        //         private static T GetInterfaceMethod<T>(IntPtr ptrInterFace, int method)
        //         {
        //             return (T)(Object)Marshal.GetDelegateForFunctionPointer(am)
        //         }

        private static T GetInterfaceMethod<T>(IntPtr ptrInterface, int method)
        {
            return (T)(Object)Marshal.GetDelegateForFunctionPointer(Marshal.ReadIntPtr(Marshal.ReadIntPtr(ptrInterface, 0 * Marshal.SizeOf(typeof(IntPtr))), method * Marshal.SizeOf(typeof(IntPtr))), typeof(T));
        }

        public static void openUsb(IntPtr ptrC)
        {
            //             fun_handler fun_callback = (fun_handler)Marshal.GetDelegateForFunctionPointer(Marshal.ReadIntPtr(Marshal.ReadIntPtr(ptrC, 0*Marshal.SizeOf(typeof(IntPtr))), 0*Marshal.SizeOf(typeof(IntPtr))),
            //                 typeof(fun_handler));
            //             string[] stringArry = new string[2];
            //             stringArry[1] = ("7806");
            //             string ss = "7806";
            //             //char[] cc = ss.ToCharArray();
            //             char pTemp[2][30]  = {"7806", "7807"};
            //             //pTemp[0] = "7806";
            //             IntPtr ptrTemp = IntPtr.Zero;
            //             bool result = fun_callback(ptrC, pTemp, 1, ptrTemp);
        }

        public static void closeUsb(IntPtr ptrC)
        {
            return;
            fun_handler2 fun_callback = GetInterfaceMethod<fun_handler2>(ptrC, 2);//(fun_handler2)Marshal.GetDelegateForFunctionPointer(Marshal.ReadIntPtr(Marshal.ReadIntPtr(ptrC, 0 * Marshal.SizeOf(typeof(IntPtr))), 2 * Marshal.SizeOf(typeof(IntPtr))),typeof(fun_handler2));
            bool result = fun_callback(ptrC);
        }

        public static void test(IntPtr ptrC)
        {
            fun_handler_test test_fun = GetInterfaceMethod<fun_handler_test>(ptrC, 0);
            string strFileName = @"D:\testFile.txt";
            IntPtr init = Marshal.StringToHGlobalAnsi(strFileName);
            //string modality = System.Runtime.InteropServices.Marshal.PtrToStringAnsi();
            byte[] byteArray = System.Text.Encoding.ASCII.GetBytes(strFileName);
            int bRes = test_fun(ptrC, strFileName);
            bRes = bRes;
        }*/

        private static string m_strData;
        private static void test_data(string pUsbData, ref Test test)
        {
            //this.setText(pUsbData);
            m_strData += pUsbData;
            mEvent.Set();
            return;
        }

        private DataCallBack m_ps;
        public bool openUsbDev()
        {
            string[] str = new string[2];
            str[0] = "7806";
            str[1] = "7805";
            m_ps = new DataCallBack(test_data);
            bool bRs = _extern_openSpecUsbDevByPid(str, 2, m_ps);
            return bRs;
        }

        public bool closeUsbDev()
        {
            return _extern_CloseUsbDev();
        }

        public Form1()
        {
            InitializeComponent();
        }

        delegate void SetTextCallBack(string strText);

        private void setText(string text)
        {
            //try
            {
                if (this.textBox1.InvokeRequired)
                {
                    while (!this.textBox1.IsHandleCreated)
                    {
                        if (this.textBox1.Disposing || this.textBox1.IsDisposed)
                        {
                            return;
                        }
                    }
                    SetTextCallBack d = new SetTextCallBack(setText);
                    this.textBox1.Invoke(d, new object[] { text });
                }
                else
                {
                    this.textBox1.Text = text;
                }
            }
            //catch (System.Exception ex)
            //{
            //	string str = ex.Message;
            //}
        }

        /*private Thread thread;
        private void test_thread_fun()
        {
            while (true)
            {
                this.setText("你好");
                System.Threading.Thread.Sleep(1000);
            }
            this.openUsbDev();
        }*/
        private bool mThreadCreateFlag = true;
        private void button1_Click(object sender, EventArgs e)
        {
            //             this.thread = new Thread(new ThreadStart(this.test_thread_fun));
            //             return;
            this.button1.Text = "正在打开设备....";
            this.button1.Enabled = false;

            if (mThreadCreateFlag)
            {
                this.thread = new Thread(new ThreadStart(this.updateTextValue));
                this.thread.Start();
                mThreadCreateFlag = false;
            }
            
            if (!openUsbDev())
            {
                this.button1.Enabled = true;
                this.button1.Text = "设备打开失败(重试)";
            }
            else
            {
                this.button1.Text = "设备打开成功....";
                this.button2.Enabled = true;
            }
        }

        private static AutoResetEvent mEvent = new AutoResetEvent(false);
        private bool m_bRuning = true;
        private Thread thread;
        private void updateTextValue()
        {
            while (m_bRuning)
            {
                
                mEvent.WaitOne();
                if (m_bRuning)
                {
                    this.setText(m_strData);
                }
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            closeUsbDev();
            this.button2.Enabled = false;
            this.button1.Enabled = true;
            this.button1.Text = "打开设备";
        }

        private void button3_Click(object sender, EventArgs e)
        {
            m_strData = "";
            this.textBox1.Text = m_strData;
        }

        public void clearResource()
        {
            if (mThreadCreateFlag == false)
            {
                m_bRuning = false;
                mEvent.Set();
                this.thread.Join();
                closeUsbDev();
            }
        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {
            this.textBox1.SelectionStart = this.textBox1.Text.Length; //Set the current caret position at the end
            this.textBox1.ScrollToCaret(); //Now scroll it automatically
        }


        private DevStatusCallBack m_devStatusCallback = null;
        private static DevStatusCallBack devStatus_Callback(int nDevStatus, IntPtr context)
        {
            sMIntStatus = nDevStatus;
            mDevStatusEvent.Set();
            return null;
        }

        private bool m_bLabelTRuning = true;
        private Thread labelthread = null;
        private static int sMIntStatus = 0;
        private void updateLabelTextValue()
        {
            while (m_bLabelTRuning)
            {

                mDevStatusEvent.WaitOne();
                if (m_bLabelTRuning)
                {
                    string strLabelStatus = "";
                    if (sMIntStatus == 1)
                    {
                        strLabelStatus = "有设备插入";
                    }
                    else
                    {
                        strLabelStatus = "有设备拔出";
                    }
                    this.setLabelText(strLabelStatus);
                }
            }
        }

        // 注册设备回调
        private void button4_Click(object sender, EventArgs e)
        {
            if (m_devStatusCallback != null)
            {
                return;
            }

            this.labelthread = new Thread(new ThreadStart(this.updateLabelTextValue));
            this.labelthread.Start();

            m_devStatusCallback = new DevStatusCallBack(devStatus_Callback);
            IntPtr p = new IntPtr(0);
            _extern_setDevStatusHandler(m_devStatusCallback, p);
        }

        private static AutoResetEvent mDevStatusEvent = new AutoResetEvent(false);
        delegate void SetLabelTextCallBack(string strDevStatus);
        private void setLabelText(string strText)
        {
            //try
            //{
                if (this.label1.InvokeRequired)
                {
                    while (!this.label1.IsHandleCreated)
                    {
                        if (this.label1.Disposing || this.label1.IsDisposed)
                        {
                            return;
                        }
                    }
                    SetLabelTextCallBack d = new SetLabelTextCallBack(setLabelText);
                    this.label1.Invoke(d, new object[] { strText });
                }
                else
                {
                    this.label1.Text = strText;
                }
           // }
        }
    }
}
