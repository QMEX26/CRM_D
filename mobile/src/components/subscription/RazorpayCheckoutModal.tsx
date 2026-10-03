import React, { useEffect } from 'react';
import {
  Modal,
  View,
  StyleSheet,
  ActivityIndicator,
  Text,
  TouchableOpacity,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { WebView } from 'react-native-webview';
import { RazorpayOrderResponse } from '../../types/subscription';

export interface RazorpaySuccessPayload {
  razorpay_order_id: string;
  razorpay_payment_id: string;
  razorpay_signature: string;
}

interface RazorpayCheckoutModalProps {
  visible: boolean;
  order: RazorpayOrderResponse | null;
  onSuccess: (data: RazorpaySuccessPayload) => void;
  onFailure: (error: any) => void;
  onClose: () => void;
}

export const RazorpayCheckoutModal: React.FC<RazorpayCheckoutModalProps> = ({
  visible,
  order,
  onSuccess,
  onFailure,
  onClose,
}) => {
  // Web checkout handler (direct Razorpay checkout.js script injection for Web)
  useEffect(() => {
    if (!visible || !order || Platform.OS !== 'web') return;

    const openWebRazorpay = () => {
      const options = {
        key: order.keyId,
        amount: order.amountInPaise,
        currency: order.currency || 'INR',
        name: 'Calling CRM',
        description: order.planName || 'CRM Subscription Plan',
        order_id: order.orderId,
        prefill: {
          name: order.userName || '',
          email: order.userEmail || '',
          contact: order.userPhone || '',
        },
        theme: {
          color: '#2563EB',
        },
        handler: function (response: any) {
          onSuccess({
            razorpay_order_id: response.razorpay_order_id,
            razorpay_payment_id: response.razorpay_payment_id,
            razorpay_signature: response.razorpay_signature,
          });
        },
        modal: {
          ondismiss: function () {
            onClose();
          },
        },
      };

      try {
        const rzp = new (window as any).Razorpay(options);
        rzp.on('payment.failed', function (response: any) {
          console.warn('Razorpay web payment failed:', response.error);
        });
        rzp.open();
      } catch (err) {
        onFailure(err);
      }
    };

    if ((window as any).Razorpay) {
      openWebRazorpay();
    } else {
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => openWebRazorpay();
      script.onerror = () => onFailure(new Error('Failed to load Razorpay checkout script'));
      document.body.appendChild(script);
    }
  }, [visible, order]);

  if (!visible || !order) return null;

  // On Web, Razorpay renders its own popup directly in DOM
  if (Platform.OS === 'web') {
    return null;
  }

  // Generate mobile HTML for WebView with official Razorpay Checkout SDK
  const checkoutHtml = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <title>Razorpay Checkout</title>
        <style>
          * { box-sizing: border-box; }
          body, html {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            background-color: #0F172A;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            color: #F8FAFC;
          }
          .loader-box {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 24px;
            border-radius: 16px;
            background: #1E293B;
            box-shadow: 0 10px 25px rgba(0,0,0,0.5);
          }
          .spinner {
            width: 44px;
            height: 44px;
            border: 4px solid #334155;
            border-top: 4px solid #38BDF8;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
            margin-bottom: 16px;
          }
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          .title {
            font-size: 16px;
            font-weight: 600;
            color: #38BDF8;
            margin-bottom: 6px;
          }
          .subtitle {
            font-size: 13px;
            color: #94A3B8;
          }
        </style>
        <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
      </head>
      <body>
        <div class="loader-box">
          <div class="spinner"></div>
          <div class="title">Calling CRM</div>
          <div class="subtitle">Opening Razorpay Checkout...</div>
        </div>

        <script>
          function postMsg(type, data) {
            if (window.ReactNativeWebView) {
              window.ReactNativeWebView.postMessage(JSON.stringify({ type: type, data: data }));
            }
          }

          var options = {
            key: "${order.keyId}",
            amount: ${Number(order.amountInPaise)},
            currency: "${order.currency || 'INR'}",
            name: "Calling CRM",
            description: "${order.planName || 'CRM Plan'}",
            order_id: "${order.orderId}",
            prefill: {
              name: "${order.userName || ''}",
              email: "${order.userEmail || ''}",
              contact: "${order.userPhone || ''}"
            },
            theme: {
              color: "#2563EB"
            },
            handler: function (response) {
              postMsg('SUCCESS', {
                razorpay_order_id: response.razorpay_order_id,
                razorpay_payment_id: response.razorpay_payment_id,
                razorpay_signature: response.razorpay_signature
              });
            },
            modal: {
              ondismiss: function() {
                postMsg('CANCEL', {});
              },
              escape: false,
              backdropclose: false
            }
          };

          window.onload = function() {
            try {
              var rzp = new Razorpay(options);
              rzp.on('payment.failed', function (resp){
                console.log('Payment failed in Razorpay UI:', resp);
              });
              rzp.open();
            } catch(e) {
              postMsg('ERROR', { message: e.message });
            }
          };
        </script>
      </body>
    </html>
  `;

  const handleMessage = (event: any) => {
    try {
      const message = JSON.parse(event.nativeEvent.data);
      if (message.type === 'SUCCESS') {
        onSuccess(message.data);
      } else if (message.type === 'CANCEL') {
        onClose();
      } else if (message.type === 'ERROR') {
        onFailure(message.data || new Error('Razorpay checkout initialization error'));
      }
    } catch (e) {
      console.error('Error parsing Razorpay WebView message:', e);
    }
  };

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={false}
      onRequestClose={onClose}
    >
      <SafeAreaView style={styles.container}>
        {/* Clean Production Header */}
        <View style={styles.header}>
          <View style={styles.headerLeft}>
            <Ionicons name="shield-checkmark" size={20} color="#38BDF8" />
            <Text style={styles.headerTitle}>Razorpay Payment Gateway</Text>
          </View>
          <TouchableOpacity onPress={onClose} style={styles.closeButton}>
            <Ionicons name="close" size={24} color="#94A3B8" />
          </TouchableOpacity>
        </View>

        <WebView
          originWhitelist={['*']}
          source={{ html: checkoutHtml, baseUrl: 'https://api.razorpay.com' }}
          onMessage={handleMessage}
          javaScriptEnabled={true}
          domStorageEnabled={true}
          startInLoadingState={true}
          renderLoading={() => (
            <View style={styles.loadingOverlay}>
              <ActivityIndicator size="large" color="#38BDF8" />
              <Text style={styles.loadingText}>Connecting to Razorpay Gateway...</Text>
            </View>
          )}
          style={styles.webView}
        />
      </SafeAreaView>
    </Modal>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F172A',
  },
  header: {
    height: 56,
    backgroundColor: '#1E293B',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#334155',
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  headerTitle: {
    color: '#F8FAFC',
    fontSize: 16,
    fontWeight: '600',
  },
  closeButton: {
    padding: 6,
  },
  webView: {
    flex: 1,
    backgroundColor: '#0F172A',
  },
  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: '#0F172A',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 10,
  },
  loadingText: {
    color: '#94A3B8',
    fontSize: 14,
    marginTop: 12,
    fontWeight: '500',
  },
});
