#include <jni.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#define MANGO210_GPIO_DEVICE_NAME "/dev/hba1c"
static int g_mango210_gpio_dev = -1;

jint Java_isens_hba1c_1analyzer_GpioPort_Open (JNIEnv * env, jobject thiz) {

//	printf("[NDK_LIB] mango210 GPIO device open start\n");
	g_mango210_gpio_dev = open(MANGO210_GPIO_DEVICE_NAME, O_RDWR|O_NDELAY);
	if(g_mango210_gpio_dev < 0) {

//		printf("[NDK_LIB] mango210 GPIO open error, dev:%d\n", g_mango210_gpio_dev);
		return -1;
	}
//	printf("[NDK_LIB] mango210 GPIO open success, dev:%d\n", g_mango210_gpio_dev);
	return 0;
}

jint Java_isens_hba1c_1analyzer_GpioPort_Close (JNIEnv * env, jobject thiz) {

//	printf("[NDK_LIB] device file close start\n");
	close(g_mango210_gpio_dev);
	g_mango210_gpio_dev = -1;
//	printf("[NDK_LIB] device file close end\n");
	return 0;
}

jint Java_isens_hba1c_1analyzer_GpioPort_GpioControl (JNIEnv * env, jobject thiz, jint gpioNum, jint gpioHighLow) {

//	printf("[NDK_LIB] mango210 GPIO %d HighLow %d\n", gpioNum, gpioHighLow);
	if(g_mango210_gpio_dev < 0) {

//		printf("[NDK_LIB] mango210 GPIO dev error, dev:%d\n", g_mango210_gpio_dev);
		return -1;
	}
	return (jint)(ioctl(g_mango210_gpio_dev, gpioNum, gpioHighLow));
}
