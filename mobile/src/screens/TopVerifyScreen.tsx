import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import axios from 'axios';

export default function TotpVerifyScreen({ navigation, route }: any) {
  const [totpCode, setTotpCode] = useState('');
  const { phone, secret } = route.params;

  const handleVerify = async () => {
    try {
      const response = await axios.post('http://<ALB-OR-SERVER-IP>:8080/api/v1/auth/totp/verify', {
        phone,
        secret,
        code: parseInt(totpCode, 10),
      });

      if (response.data.token) {
        // Navigate based on role returned from backend RBAC[cite: 2, 4]
        const target = response.data.role === 'HELPER' ? 'HelperDashboard' : 'CustomerHome';
        navigation.replace(target, { token: response.data.token });
      }
    } catch {
      Alert.alert('Verification Failed', 'Invalid TOTP code from Authenticator app.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Authenticator Verification</Text>
      <Text style={styles.subtitle}>Enter the 6-digit code from Google or Microsoft Authenticator</Text>
      <TextInput
        style={styles.input}
        keyboardType="numeric"
        maxLength={6}
        placeholder="123456"
        value={totpCode}
        onChangeText={setTotpCode}
      />
      <TouchableOpacity style={styles.button} onPress={handleVerify}>
        <Text style={styles.buttonText}>Confirm & Login</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: '#F8FAFC' },
  title: { fontSize: 22, fontWeight: '700', color: '#0F172A', marginBottom: 8 },
  subtitle: { fontSize: 14, color: '#64748B', marginBottom: 24 },
  input: { backgroundColor: '#FFF', borderWidth: 1, borderColor: '#CBD5E1', borderRadius: 8, padding: 14, fontSize: 18, textAlign: 'center', letterSpacing: 8 },
  button: { marginTop: 20, backgroundColor: '#2563EB', borderRadius: 8, padding: 14, alignItems: 'center' },
  buttonText: { color: '#FFF', fontWeight: '600', fontSize: 16 }
});
